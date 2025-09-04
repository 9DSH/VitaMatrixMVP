package com.swings.vitamatrix.questions

data class Question(
    val label: String,
    val questionText: String,
    val type: QuestionType,
    val negativeAnswer: String? = null,
    val options: List<String> = emptyList(),
//    val canSkip: Boolean = false
)

enum class QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TEXT,
    FINISHING
}
