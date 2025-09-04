//package com.swings.vitamatrix.chat
//
//import RetrofitClient
//import android.content.Intent
//import android.os.Bundle
//import android.widget.EditText
//import android.widget.ImageButton
//import android.widget.TextView
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.swings.vitamatrix.R
//import com.swings.vitamatrix.api.ChatRequest
//import com.swings.vitamatrix.api.ChatResponse
//import com.swings.vitamatrix.db.AppDatabase
//import com.swings.vitamatrix.db.DatabaseProvider
//import com.swings.vitamatrix.models.Conversation
//import com.swings.vitamatrix.models.Message
//import com.swings.vitamatrix.questions.WizardActivity
//import com.swings.vitamatrix.utils.ToastType
//import com.swings.vitamatrix.utils.Util
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class MessageListActivity : AppCompatActivity() {
//
//    private val messages = mutableListOf<Message>()
//    private lateinit var adapter: MessageListAdapter
//    private lateinit var recycler: RecyclerView
//    private lateinit var send: ImageButton
//    private lateinit var messageInput: EditText
//    private lateinit var improve: TextView
//    private var conversationId: Long? = null
//    private var questionAnswers: String? = null
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        val position =
//            (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//        outState.putInt("scroll_position", position)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        val position = savedInstanceState.getInt("scroll_position", 0)
//        recycler.scrollToPosition(position)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_message_list)
//
//        initializeViews()
//        setupRecyclerView()
//        loadConversationOrCreateNew()
//        loadQA()
//
//        improve.setOnClickListener {
//            startActivity(Intent(this, WizardActivity::class.java))
//        }
//
//        improve.setOnLongClickListener {
//
//            showConfirmationDialog()
//            true
//        }
//
//        send.setOnClickListener {
//            val question = messageInput.text.toString().trim()
//            if (question.isNotEmpty()) {
//                sendMessageToChatGPT(question)
//            }
//        }
//    }
//
//    private fun showConfirmationDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("تایید حذف")
//        builder.setMessage("آیا مایل هستید اطلاعات شخصی خود را حذف کنید؟\n این اطلاعات به بهود جواب های هوش مصنوعی کمک می کند. ")
//        builder.setPositiveButton("Yes") { _, _ ->
//            val qaDao = getDB().qaDao()
//            CoroutineScope(Dispatchers.IO).launch {
//                withContext(Dispatchers.Main) {
//                    qaDao.deleteQADataById(1)
//                }
//            }
//        }
//
//        builder.setNegativeButton("No") { dialog, _ ->
//
//            dialog.dismiss()
//        }
//        builder.create().show()
//    }
//
//    private fun getDB(): AppDatabase {
//        return DatabaseProvider.getDatabase(this);
//    }
//
//    private fun initializeViews() {
////        improve = findViewById(R.id.improve)
//        recycler = findViewById(R.id.recycler)
//        messageInput = findViewById(R.id.messageInput)
//        send = findViewById(R.id.send)
//    }
//
//    private fun setupRecyclerView() {
//        adapter = MessageListAdapter(messages)
//        recycler.layoutManager = LinearLayoutManager(this)
//        recycler.adapter = adapter
//    }
//
//    private fun loadConversationOrCreateNew() {
//        val conversationDao = getDB().conversationDao()
//        val messageDao = getDB().messageDao()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val conversations = conversationDao.getAllConversations()
//            if (conversations.isNotEmpty()) {
//                conversationId = conversations.last().id.toLong()
//
//                withContext(Dispatchers.Main) {
//                    messageDao.getMessagesByConversationIdLive(conversationId!!.toInt())
//                        .observe(this@MessageListActivity) { liveMessages ->
//                            messages.clear()
//                            messages.addAll(liveMessages)
//                            adapter.notifyDataSetChanged()
//                            recycler.scrollToPosition(messages.size - 1)
//                        }
//                }
//            } else {
//                CoroutineScope(Dispatchers.IO).launch {
//                    conversationId = conversationDao.insertConversation(
//                        Conversation(
//                            title = "New Conversation",
//                            timestamp = System.currentTimeMillis()
//                        )
//                    )
//                    withContext(Dispatchers.Main) {
//                        messageDao.getMessagesByConversationIdLive(conversationId!!.toInt())
//                            .observe(this@MessageListActivity) { liveMessages ->
//                                messages.clear()
//                                messages.addAll(liveMessages)
//                                adapter.notifyDataSetChanged()
//                                recycler.scrollToPosition(messages.size - 1)
//                            }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun loadQA() {
//        val qaDao = getDB().qaDao()
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.Main) {
//                qaDao.getLiveQAData()
//                    .observe(this@MessageListActivity) { qa ->
//                        questionAnswers = qa
//                        if (qa == null)
//                            improve.text = "Improve questions"
//                        else
//                            improve.text = "Improved!"
//                    }
//            }
//        }
//    }
//
//
//
//    private fun addMessageToDB(message: Message) {
//        val messageDao = getDB().messageDao()
//        CoroutineScope(Dispatchers.IO).launch {
//            messageDao.insertMessage(message)
//        }
//    }
//
//    private fun addToMessages(text: String, isGPT: Boolean) {
//        val sender = if (isGPT) "gpt" else "user"
//        val message = Message(conversationId!!.toInt(), sender, text)
//        addMessageToDB(message)
//
//        if (!isGPT) {
//            messageInput.text.clear()
//        }
//    }
//
//
//    private fun sendMessageToChatGPT(question: String) {

//        val promptContent = if (!questionAnswers.isNullOrEmpty()) {
//            "\n:تو یک پزشک هستی.کاربر به این سوالات در مورد خودش جواب داده است$questionAnswers \nبا توجه به این اطلاعات  به این سوال جواب بده\n$question"
//        } else {
//            question
//        }
//
//
//        val request = ChatRequest(
//            model = "gpt-4",
//            messages = listOf(
//                mapOf("role" to "system", "content" to "تو یک پزشک هستی"),
//                mapOf("role" to "user", "content" to promptContent)
//            )
//        )
//
//
//        addToMessages(question, false)
//
//        RetrofitClient.api.sendMessage(request).enqueue(object : Callback<ChatResponse> {
//            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
//                if (response.isSuccessful) {
//                    val reply = response.body()?.choices?.first()?.message?.content ?: "No response"
//
//                    addToMessages(reply, true)
//                } else {
//                    Util.toast(ToastType.ERROR, "Error: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
//                Util.toast(ToastType.ERROR, "Error: ${t.message}")
//            }
//        })
//    }
//
//}
