package com.swings.vitamatrix.main

import RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.swings.vitamatrix.api.ChatRequest
import com.swings.vitamatrix.api.ChatResponse
import com.swings.vitamatrix.databinding.FragmentHealthBinding
import com.swings.vitamatrix.db.AppDatabase
import com.swings.vitamatrix.db.DatabaseProvider
import com.swings.vitamatrix.models.QAData
import com.swings.vitamatrix.questions.WizardActivity
import com.swings.vitamatrix.utils.HealthRecommendation
import com.swings.vitamatrix.utils.SupplementView
import com.swings.vitamatrix.utils.ToastType
import com.swings.vitamatrix.utils.UserData
import com.swings.vitamatrix.utils.Util
import com.swings.vitamatrix.utils.VitaMatrixPrompter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HealthFragment : Fragment() {

    private var questionAnswers: String? = null


    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.reload.setOnClickListener { sendMessageToChatGPT() }
        loadQA()

    }



    private fun getDB(): AppDatabase {
        return DatabaseProvider.getDatabase(requireContext())
    }


    private fun loadQA() {
        try {
            val qaDao = getDB().qaDao()
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    if (view != null)
                        qaDao.getLiveQAData()
                            .observe(viewLifecycleOwner) { qa ->
                                questionAnswers = qa
                                if (qa == null)
                                    startActivity(Intent(requireContext(), WizardActivity::class.java))
                                else {
                                    try {
                                        val userData = Gson().fromJson(questionAnswers, UserData::class.java)
                                        binding.goal.text = userData.goal
                                    } catch (e: Exception) {

                                    }

                                }
                            }

                    qaDao.getLiveHealthJson()
                        .observe(viewLifecycleOwner) { json ->
                            try {
                                val healthRecommendation = Gson().fromJson(json,
                                    HealthRecommendation::class.java)
                                if (healthRecommendation == null) {
                                    binding.reload.performClick()
                                } else {
                                    parseHealthJson(healthRecommendation)
                                }
                            } catch (e: Exception) {
                                null
                            }


//                        else
//                            binding.improve.text = "Improved!"
                        }
                }
            }
        } catch (e: Exception) {
            loadQA()
        }
    }


    private fun sendMessageToChatGPT() {
        if (questionAnswers.isNullOrEmpty()) {
            Util.toast(ToastType.ERROR, "To use this section, you must first answer personal questions.")
            return
        }
        val promptContent = VitaMatrixPrompter.getPrompt() + questionAnswers

        val request = ChatRequest(
            model = "gpt-4",
            messages = listOf(
                mapOf("role" to "system", "content" to "You are a doctor"),
                mapOf("role" to "user", "content" to promptContent)
            )
        )
        binding.progress.visibility = View.VISIBLE
        binding.reload.visibility = View.INVISIBLE
        RetrofitClient.api.sendMessage(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.first()?.message?.content ?: "No response"
                    Util.log("res = $reply")


                    CoroutineScope(Dispatchers.IO).launch {
                        getDB().qaDao().insertQAData(QAData(2, jsonData = reply))
                    }

                } else {
                    Util.toast(ToastType.ERROR, "Error: ${response.errorBody()?.string()}")
                }
                binding.progress.visibility = View.INVISIBLE
                binding.reload.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread

                    Util.toast(ToastType.ERROR, "Error: ${t.message ?: "Unknown error"}")
                    binding.progress.visibility = View.INVISIBLE
                    binding.reload.visibility = View.VISIBLE
                }
            }
        })
    }

    fun parseHealthJson(healthRecommendation: HealthRecommendation) {
        binding.progress.visibility = View.INVISIBLE
        binding.reload.visibility = View.INVISIBLE
        binding.healthPanel.visibility = View.VISIBLE

        binding.carbs.text = healthRecommendation.nutrients.carbs.replace("g", "")
        binding.fats.text = healthRecommendation.nutrients.fats.replace("g", "")
        binding.fiber.text = healthRecommendation.nutrients.fiber.replace("g", "")
        binding.protein.text = healthRecommendation.nutrients.protein.replace("g", "")

        if (healthRecommendation.foods.isNotEmpty())
            binding.food1.text = healthRecommendation.foods[0]
        if (healthRecommendation.foods.size > 1)
            binding.food2.text = healthRecommendation.foods[1]
        if (healthRecommendation.foods.size > 2)
            binding.food3.text = healthRecommendation.foods[2]

        healthRecommendation.supplements.forEach { supplement ->
            val supplementView = SupplementView(requireContext())
            supplementView.bind(supplement)
            binding.supplement.addView(supplementView)
        }

        healthRecommendation.medications.forEach { supplement ->
            val supplementView = SupplementView(requireContext())
            supplementView.bind(supplement)
            binding.medications.addView(supplementView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
