package com.swings.vitamatrix.chat

import RetrofitClient
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.swings.vitamatrix.api.ChatRequest
import com.swings.vitamatrix.api.ChatResponse
import com.swings.vitamatrix.databinding.ActivityMessageListBinding
import com.swings.vitamatrix.db.AppDatabase
import com.swings.vitamatrix.db.DatabaseProvider
import com.swings.vitamatrix.models.Conversation
import com.swings.vitamatrix.models.Message
import com.swings.vitamatrix.utils.ToastType
import com.swings.vitamatrix.utils.Util
import com.swings.vitamatrix.utils.VitaMatrixPrompter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageListFragment : Fragment() {

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageListAdapter
    private var conversationId: Long? = null
    private var questionAnswers: String? = null


    private var _binding: ActivityMessageListBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val position =
            (binding.recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        outState.putInt("scroll_position", position)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val position = savedInstanceState?.getInt("scroll_position", 0) ?: 0
        binding.recycler.scrollToPosition(position)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadConversationOrCreateNew()
        loadQA()

//        binding.improve.setOnClickListener {
//            startActivity(Intent(requireContext(), WizardActivity::class.java))
//        }
//
//        binding.improve.setOnLongClickListener {
//
//            showConfirmationDialog()
//            true
//        }

        binding.send.setOnClickListener {
            val question = binding.messageInput.text.toString().trim()
            if (question.isNotEmpty()) {
                sendMessageToChatGPT(question)
            }
        }
    }



    private fun getDB(): AppDatabase {
        return DatabaseProvider.getDatabase(requireContext());
    }

    private fun setupRecyclerView() {
        adapter = MessageListAdapter(messages)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
    }

    private fun loadConversationOrCreateNew() {
        val conversationDao = getDB().conversationDao()
        val messageDao = getDB().messageDao()

        CoroutineScope(Dispatchers.IO).launch {
            val conversations = conversationDao.getAllConversations()
            if (conversations.isNotEmpty()) {
                conversationId = conversations.last().id.toLong()

                withContext(Dispatchers.Main) {
                    messageDao.getMessagesByConversationIdLive(conversationId!!.toInt())
                        .observe(viewLifecycleOwner) { liveMessages ->
                            messages.clear()
                            messages.addAll(liveMessages)
                            adapter.notifyDataSetChanged()
                            binding.recycler.scrollToPosition(messages.size - 1)
                        }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    conversationId = conversationDao.insertConversation(
                        Conversation(
                            title = "New Conversation",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    withContext(Dispatchers.Main) {
                        messageDao.getMessagesByConversationIdLive(conversationId!!.toInt())
                            .observe(viewLifecycleOwner) { liveMessages ->
                                messages.clear()
                                messages.addAll(liveMessages)
                                adapter.notifyDataSetChanged()
                                binding.recycler.scrollToPosition(messages.size - 1)
                            }
                    }
                }
            }
        }
    }

    private fun loadQA() {
        val qaDao = getDB().qaDao()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                qaDao.getLiveQAData()
                    .observe(viewLifecycleOwner) { qa ->
                        questionAnswers = qa
//                        if (qa == null)
//                            binding.improve.text = "Improve questions"
//                        else
//                            binding.improve.text = "Improved!"
                    }
            }
        }
    }

    private fun addMessageToDB(message: Message) {
        val messageDao = getDB().messageDao()
        CoroutineScope(Dispatchers.IO).launch {
            messageDao.insertMessage(message)
        }
    }

    private fun addToMessages(text: String, isGPT: Boolean) {
        val sender = if (isGPT) "gpt" else "user"
        val message = Message(conversationId!!.toInt(), sender, text)
        addMessageToDB(message)

        if (!isGPT) {
            binding.messageInput.text.clear()
        }
    }


    private fun sendMessageToChatGPT(question: String) {
        val promptContent = if (!questionAnswers.isNullOrEmpty()) {
            "The user has answered these questions about himself $questionAnswers \nAnswer this question based on this information\n$question"
        } else {
            question
        }


        val request = ChatRequest(
            model = "gpt-4",
            messages = listOf(
                mapOf("role" to "system", "content" to VitaMatrixPrompter.getQuestionPrompt()),
                mapOf("role" to "user", "content" to promptContent),
            )
        )


        addToMessages(question, false)

        RetrofitClient.api.sendMessage(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.first()?.message?.content ?: "No response"
                    addToMessages(reply, true)
                } else {
                    Util.toast(ToastType.ERROR, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Util.toast(ToastType.ERROR, "Error: ${t.message}")
            }
        })
    }

}
