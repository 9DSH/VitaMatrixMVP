package com.swings.vitamatrix.questions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.swings.vitamatrix.utils.Util
import java.util.Locale

class ConversationalHealthAssessment : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val REQUEST_CODE_SPEECH_INPUT = 101

    private val questions: MutableList<Question> = mutableListOf(
        Question("age", "How old are you?", QuestionType.TEXT),
        Question("weight", "What is your weight in kilograms?", QuestionType.TEXT),
        Question("height", "What is your height in centimeters?", QuestionType.TEXT),
        Question("exercise", "How often do you exercise per week?", QuestionType.SINGLE_CHOICE,
            options = listOf("Never", "1-2 times", "3-4 times", "5+ times")),
        Question("diet", "Do you follow a specific diet?", QuestionType.SINGLE_CHOICE,
            options = listOf("No diet", "Vegetarian", "Vegan", "Keto", "Other")),
        Question("conditions", "Do you have any existing medical conditions?", QuestionType.MULTIPLE_CHOICE,
            options = listOf("Diabetes", "Hypertension", "High Cholesterol", "Arthritis", "None")),
        Question("finish", "That’s the end of the assessment. Thank you!", QuestionType.FINISHING)
    )

    private var currentQuestionIndex = 0
    private val answers: MutableMap<String, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Start the first question
        askNextQuestion()
    }

    private fun askNextQuestion() {
        if (currentQuestionIndex >= questions.size) {
            finishAssessment()
            return
        }

        val question = questions[currentQuestionIndex]
        Toast.makeText(this, question.questionText, Toast.LENGTH_LONG).show()
        Util.log("Asking: ${question.questionText}")

        listenForAnswer(question)
    }

    private fun listenForAnswer(question: Question) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, question.questionText)
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""

            val question = questions[currentQuestionIndex]
            answers[question.label] = spokenText
            Util.log("Answer recorded: ${question.label} -> $spokenText")

            currentQuestionIndex++
            askNextQuestion()
        }
    }

    private fun finishAssessment() {
        Util.log("Assessment Finished. Answers: $answers")
        Toast.makeText(this, "Assessment Completed!", Toast.LENGTH_LONG).show()

        // TODO: Save answers to DB or send to server
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
