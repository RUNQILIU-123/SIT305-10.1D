package com.example.llmlearningassistant.data.model

data class User(
    val id: String = "1",
    val username: String = "Student",
    val email: String = "student@example.com",
    val selectedInterests: List<String> = emptyList()
)

data class Interest(
    val id: String,
    val name: String,
    var isSelected: Boolean = false
)

data class LearningTask(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val questions: List<QuizQuestion> = emptyList()
)

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val correctAnswer: String,
    val options: List<String> = emptyList()
)

data class Flashcard(
    val question: String,
    val answer: String
)

data class LLMResponse(
    val prompt: String,
    val response: String
)

data class ExplanationResponse(
    val evaluation: String,
    val explanation: String,
    val suggestion: String
)
