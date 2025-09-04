package com.swings.vitamatrix.utils

import com.swings.vitamatrix.questions.Question
import com.swings.vitamatrix.questions.QuestionType

object Constants {

    const val FULL_NAME = "Full Name"
    const val WEIGHT = "Weight"
    const val HEIGHT = "Height"
    const val AGE = "Age"
    const val GOAL = "Primary Health Goal"

    val QUESTIONS = listOf(
        Question(FULL_NAME, "What is your full name?", QuestionType.TEXT),
        Question("Sex", "What is your sex?", QuestionType.SINGLE_CHOICE,
            options = listOf("Male", "Female")),
        Question(WEIGHT, "What’s your weight?", QuestionType.TEXT),
        Question(HEIGHT, "How tall are you?", QuestionType.TEXT),
        Question(AGE, "How old are you?", QuestionType.TEXT),
        Question("Overall Health Rating", "How would you rate your overall health?",
            QuestionType.SINGLE_CHOICE, options = listOf("Excellent", "Good", "Fair", "Poor")),
        Question("Physical Activity Level", "How often do you engage in physical exercise?",
            QuestionType.SINGLE_CHOICE, options = listOf("Daily", "Several times a week", "Occasionally", "Rarely or never")),
        Question("Sleep Duration", "How many hours of sleep do you typically get per night?",
            QuestionType.SINGLE_CHOICE, options = listOf("Less than 5 hours", "5-6 hours", "7-8 hours", "More than 8 hours")),
        Question("Smoking/Tobacco Use", "Do you smoke or use tobacco products?",
            QuestionType.SINGLE_CHOICE, options = listOf("Yes, regularly", "Yes, occasionally", "No, I quit", "No, I never have")),
        Question("Daily Fruit & Vegetable Intake", "On average, how many servings of fruits and vegetables do you consume daily?", QuestionType.SINGLE_CHOICE,
            options = listOf("Less than 1 serving", "1-2 servings", "3-4 servings", "5 or more servings")),
        Question("Chronic Conditions", "Please list any chronic illnesses or conditions you have been diagnosed with:\n" +
                "(e.g., diabetes, hypertension, asthma, etc.)", QuestionType.TEXT, negativeAnswer = "I have no illness"),
        Question("Current Symptoms", "Have you experienced any of the following symptoms in the past month? (Select all that apply)", QuestionType.MULTIPLE_CHOICE,
            options = listOf("Unexplained weight loss or gain", "Persistent fatigue or low energy",
                "Shortness of breath", "Chest pain", "Frequent headaches", "Digestive issues", "None of the above")),
        Question("Medications & Supplements", "Are you currently taking any medications, including over-the-counter or supplements?", QuestionType.TEXT,
            negativeAnswer = "No, I don't take medication"),
        Question("Mental Health Status", "Over the past two weeks, how often have you felt down, depressed, or hopeless?", QuestionType.SINGLE_CHOICE,
            options = listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
        Question("Recent Stressors", "Please describe any significant stressors or changes in your life recently that may have impacted your health:", QuestionType.TEXT),
        Question(
            GOAL, "What is your primary health goal?", QuestionType.SINGLE_CHOICE,
            options = listOf("Energizer", "Fat Burner", "Muscle Builder", "Joint Saver", "Deep Sleeper", "Heart Guard", "Immune Shield", "Mind Booster", "Gut Fixer", "Sugar Balancer")),
        Question("", "", QuestionType.FINISHING),
    )
}