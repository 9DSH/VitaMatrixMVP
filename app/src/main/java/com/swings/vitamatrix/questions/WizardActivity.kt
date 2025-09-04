package com.swings.vitamatrix.questions

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.swings.vitamatrix.databinding.WizardActivityBinding
import com.swings.vitamatrix.db.DatabaseProvider
import com.swings.vitamatrix.models.QAData
import com.swings.vitamatrix.utils.Constants.QUESTIONS
import com.swings.vitamatrix.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WizardActivity : AppCompatActivity() {



    private lateinit var adapter: QuestionAdapter
    private val answersMap = mutableMapOf<String, String?>()


    private lateinit var binding: WizardActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WizardActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = QuestionAdapter(QUESTIONS,
            onFinished = {
                finishWizard()
            },
            onAnswerSubmitted = { question, answer ->
                answersMap[question] = answer
                Util.log("$question $answer")
                binding.viewPager.currentItem += 1
            })

        binding.viewPager.adapter = adapter


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageNumber(position)
            }
        })
    }

    private fun updatePageNumber(position: Int) {
        val totalPages = adapter.itemCount - 1
        val page = position + 1
        if (page <= totalPages) {
            binding.questionProgressText.visibility = View.VISIBLE
            binding.questionProgressbar.visibility = View.VISIBLE
            binding.questionProgressText.text = "QUESTION $page OF $totalPages"
            binding.questionProgressbar.progress = page
            binding.questionProgressbar.max = totalPages
        } else {
            binding.questionProgressText.visibility = View.INVISIBLE
            binding.questionProgressbar.visibility = View.INVISIBLE
        }
    }

    private fun finishWizard() {
        val db = DatabaseProvider.getDatabase(this)
        val qaDataDao = db.qaDao()

        val gson = Gson()
        val jsonString = gson.toJson(answersMap)

        Util.log(jsonString)

        CoroutineScope(Dispatchers.IO).launch {
            qaDataDao.insertQAData(QAData(1, jsonData = jsonString))
        }

        finish()
    }
}
