package com.swings.vitamatrix.questions

import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.swings.vitamatrix.R
import com.swings.vitamatrix.utils.ToastType
import com.swings.vitamatrix.utils.Util

class UpdateQuestionAdapter(
    private var qa: String?,
    private val questions: List<Question>,
    private val onAnswerSubmitted: (String, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SINGLE_CHOICE = 1
        private const val TYPE_MULTIPLE_CHOICE = 2
        private const val TYPE_TEXT = 3
        private const val TYPE_FINISHING = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (questions[position].type) {
            QuestionType.SINGLE_CHOICE -> TYPE_SINGLE_CHOICE
            QuestionType.MULTIPLE_CHOICE -> TYPE_MULTIPLE_CHOICE
            QuestionType.TEXT -> TYPE_TEXT
            QuestionType.FINISHING -> TYPE_FINISHING
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SINGLE_CHOICE -> SingleChoiceViewHolder(
                inflater.inflate(R.layout.item_update_single_choice, parent, false)
            )
            TYPE_MULTIPLE_CHOICE -> MultipleChoiceViewHolder(
                inflater.inflate(R.layout.item_update_multiple_choice, parent, false)
            )
            TYPE_TEXT -> TextQuestionViewHolder(
                inflater.inflate(R.layout.item_update_text, parent, false)
            )

            else -> TextQuestionViewHolder(
                inflater.inflate(R.layout.item_question_text, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = questions[position]
        when (holder) {
            is SingleChoiceViewHolder -> holder.bind(question, position)
            is MultipleChoiceViewHolder -> holder.bind(question, position)
            is TextQuestionViewHolder -> holder.bind(question, position)
        }
    }

    override fun getItemCount(): Int = questions.size - 1

    inner class SingleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGroupOptions)
        private val number: TextView = itemView.findViewById(R.id.number)

        fun bind(question: Question, position: Int) {
            questionText.text = question.questionText
            number.text = "Question ${position + 1}"


            // 1. حذف Listener قبلی برای جلوگیری از تداخل
            radioGroup.setOnCheckedChangeListener(null)

            // 2. پاک کردن تمام گزینه‌های قبلی
            radioGroup.removeAllViews()


            val value = getValue(question.label)

            question.options.forEach { option ->
                val radioButton = RadioButton(
                    ContextThemeWrapper(itemView.context, R.style.CustomCheckBox),
                    null,
                    R.style.CustomCheckBox
                ).apply {
                    id = View.generateViewId()
                    text = option
                    isChecked = (value == option)
                }
                radioGroup.addView(radioButton)
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId != -1) {
                    val selectedOption =
                        itemView.findViewById<RadioButton>(checkedId).text.toString()
                    Util.log("selected option = $selectedOption")
                    onAnswerSubmitted(question.label, selectedOption)
                    setValue(question.label, selectedOption)
                }
            }
        }
    }

    inner class MultipleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val checkboxGroup: LinearLayout = itemView.findViewById(R.id.checkboxGroup)
        private val number: TextView = itemView.findViewById(R.id.number)

        fun bind(question: Question, position: Int) {
            questionText.text = question.questionText
            number.text = "Question ${position + 1}"
            checkboxGroup.removeAllViews()
            val selectedOptions = mutableListOf<String>()
            val value = getValue(question.label)

            question.options.forEach { option ->
                val checkBox = CheckBox(ContextThemeWrapper(itemView.context, R.style.CustomCheckBox),
                    null,
                    R.style.CustomCheckBox
                ).apply {
                    text = option
                    Util.log("text = $option")
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedOptions.add(option) else selectedOptions.remove(option)
                        val answer = selectedOptions
                            .filter { it.isNotBlank() }
                            .joinToString(separator = ", ")
                        onAnswerSubmitted(question.label, answer)
                        setValue(question.label, answer)
                    }
                    if (value != null && value.contains(option))
                        isChecked = true
                }
                checkboxGroup.addView(checkBox)
            }
        }
    }

    fun getValue(key: String): String? {
        return try {
            val jsonObject = JsonParser.parseString(qa).asJsonObject
            if (jsonObject.has(key)) {
                jsonObject.get(key).asString
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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

    inner class TextQuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val editTextAnswer: EditText = itemView.findViewById(R.id.editTextAnswer)
        private val negativeAnswer: CheckBox = itemView.findViewById(R.id.negativeAnswer)
        private val number: TextView = itemView.findViewById(R.id.number)
        private val submitButton: ImageButton = itemView.findViewById(R.id.submitButton)

        fun bind(question: Question, position: Int) {
            questionText.text = question.questionText
            number.text = "Question ${position + 1}"
            val value = getValue(question.label)
            negativeAnswer.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked)
                    return@setOnCheckedChangeListener

                onAnswerSubmitted(question.label, negativeAnswer.text.toString())
                setValue(question.label, negativeAnswer.text.toString())
            }

            editTextAnswer.apply {
                setText("$value")
                inputType = InputType.TYPE_CLASS_TEXT
                imeOptions = EditorInfo.IME_ACTION_DONE

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        negativeAnswer.isChecked = false
                    }
                }

                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        negativeAnswer.isChecked = false
                    }
                    false
                }


                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val answer = editTextAnswer.text.toString()
                        if (answer.isNotEmpty()) {
                            onAnswerSubmitted(question.label, answer)
                            setValue(question.label, answer)
                        } else {
                            Toast.makeText(itemView.context, "Please enter your answer", Toast.LENGTH_SHORT)
                                .show()
                        }
                        true
                    } else {
                        false
                    }
                }
            }

            if (question.negativeAnswer != null) {
                negativeAnswer.visibility = View.VISIBLE
                negativeAnswer.text = question.negativeAnswer
                if (value != null && value.contains(question.negativeAnswer)) {
                    negativeAnswer.isChecked = true
                    editTextAnswer.setText("")
                }
            } else {
                negativeAnswer.visibility = View.GONE
            }



            submitButton.setOnClickListener {
                val answer = editTextAnswer.text.toString()
                onAnswerSubmitted(question.label, answer)
                setValue(question.label, answer)
                Util.toast(ToastType.SUCCESS, "Saved!")
            }
        }
    }
}
