package com.swings.vitamatrix.questions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonParser
import com.swings.vitamatrix.databinding.ActivityUpdateAnswersBinding
import com.swings.vitamatrix.db.DatabaseProvider
import com.swings.vitamatrix.models.QAData
import com.swings.vitamatrix.utils.Constants.QUESTIONS
import com.swings.vitamatrix.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateAnswersActivity : AppCompatActivity() {

    private lateinit var adapter: UpdateQuestionAdapter
    var qa: String? = null


    private lateinit var binding: ActivityUpdateAnswersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAnswersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        qa = intent.getStringExtra("qa")

        setupRecyclerView(qa)

        binding.update.setOnClickListener {
            finishWizard()
        }
    }

    private fun setupRecyclerView(qa: String?) {
        adapter = UpdateQuestionAdapter(qa, QUESTIONS,
            onAnswerSubmitted = { question, answer ->
                setValue(question, answer)
            })
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    fun setValue(key: String, newValue: String) {
        return try {
            val jsonObject = JsonParser.parseString(qa).asJsonObject
            jsonObject.addProperty(key, newValue)
            qa = jsonObject.toString()
            Util.log("$qa")
        } catch (e: Exception) {
            Util.log(e)
        }
    }



    private fun finishWizard() {
        val db = DatabaseProvider.getDatabase(this)
        val qaDataDao = db.qaDao()

        if (!qa.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                qaDataDao.insertQAData(QAData(1, jsonData = qa!!))
                withContext(Dispatchers.Main) {
                    qaDataDao.deleteHealthJson()
                }
            }

        }


        finish()
    }
}
