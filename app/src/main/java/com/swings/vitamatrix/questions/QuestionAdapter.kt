package com.swings.vitamatrix.questions

import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.swings.vitamatrix.R
import com.swings.vitamatrix.utils.Util

class QuestionAdapter(
    private val questions: List<Question>,
    private val onFinished: () -> Unit,
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
                inflater.inflate(R.layout.item_question_single_choice, parent, false)
            )
            TYPE_MULTIPLE_CHOICE -> MultipleChoiceViewHolder(
                inflater.inflate(R.layout.item_question_multiple_choice, parent, false)
            )
            TYPE_TEXT -> TextQuestionViewHolder(
                inflater.inflate(R.layout.item_question_text, parent, false)
            )
            TYPE_FINISHING -> FinishingViewHolder(
                inflater.inflate(R.layout.item_finishing, parent, false)
            )

            else -> TextQuestionViewHolder(
                inflater.inflate(R.layout.item_question_text, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = questions[position]
        when (holder) {
            is SingleChoiceViewHolder -> holder.bind(question)
            is MultipleChoiceViewHolder -> holder.bind(question)
            is TextQuestionViewHolder -> holder.bind(question)
            is FinishingViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = questions.size

    inner class SingleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGroupOptions)

        fun bind(question: Question) {
            questionText.text = question.questionText
            radioGroup.removeAllViews()
            question.options.forEach { option ->
                val radioButton = RadioButton(
                    ContextThemeWrapper(itemView.context, R.style.CustomCheckBox),
                    null,
                    R.style.CustomCheckBox
                ).apply {
                    text = option
                }
                radioGroup.addView(radioButton)
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId != -1) {
                    val selectedOption =
                        itemView.findViewById<RadioButton>(checkedId).text.toString()
                    onAnswerSubmitted(question.label, selectedOption)
                } else {
                    Toast.makeText(itemView.context, "Please select an option", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    inner class MultipleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val checkboxGroup: LinearLayout = itemView.findViewById(R.id.checkboxGroup)
        private val nextButton: Button = itemView.findViewById(R.id.nextButton)

        fun bind(question: Question) {
            questionText.text = question.questionText
            checkboxGroup.removeAllViews()
            val selectedOptions = mutableListOf<String>()

            question.options.forEach { option ->
                val checkBox = CheckBox(ContextThemeWrapper(itemView.context, R.style.CustomCheckBox),
                    null,
                    R.style.CustomCheckBox
                ).apply {
                    text = option
                    Util.log("text = $option")
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedOptions.add(option) else selectedOptions.remove(option)

                        Util.log("count = ${selectedOptions.size}")
                    }
                }
                checkboxGroup.addView(checkBox)
            }

            nextButton.setOnClickListener {
                val answer = selectedOptions
                    .filter { it.isNotBlank() }
                    .joinToString(separator = ", ")
                onAnswerSubmitted(question.label, answer)
            }
        }
    }

    inner class TextQuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val editTextAnswer: EditText = itemView.findViewById(R.id.editTextAnswer)
        private val negativeAnswer: CheckBox = itemView.findViewById(R.id.negativeAnswer)

        fun bind(question: Question) {
            questionText.text = question.questionText
            editTextAnswer.apply {
                setText("")
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
            } else {
                negativeAnswer.visibility = View.INVISIBLE
            }

            negativeAnswer.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked)
                    return@setOnCheckedChangeListener

                onAnswerSubmitted(question.label, negativeAnswer.text.toString())
            }
        }
    }



    inner class FinishingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val finish: TextView = itemView.findViewById(R.id.finish)

        fun bind() {
            finish.setOnClickListener {
                onFinished()
            }

        }
    }
}
