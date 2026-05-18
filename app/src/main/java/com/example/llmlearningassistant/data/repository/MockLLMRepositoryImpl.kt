package com.example.llmlearningassistant.data.repository

import com.example.llmlearningassistant.data.model.LLMResponse
import kotlinx.coroutines.delay

class MockLLMRepositoryImpl : LLMRepository {
    override suspend fun explainAnswer(
        question: String,
        correctAnswer: String,
        userAnswer: String
    ): Result<LLMResponse> {
        delay(1500) // Simulate network delay
        val isCorrect = correctAnswer.equals(userAnswer, ignoreCase = true)
        val prompt = "Explain why '$userAnswer' is ${if (isCorrect) "correct" else "incorrect"} for the question: '$question'. The correct answer is '$correctAnswer'."
        val response = if (isCorrect) {
            "Excellent job! Your answer '$userAnswer' is correct. This concept relates to how data is structured in memory, ensuring efficient access. Keep up the great work!"
        } else {
            "Actually, the correct answer is '$correctAnswer'. While '$userAnswer' is a common misconception, '$correctAnswer' is preferred because it handles edge cases more robustly in this specific scenario. Suggestion: Review the basics of this topic."
        }
        return Result.success(LLMResponse(prompt, response))
    }

    override suspend fun generateFlashcards(topic: String): Result<LLMResponse> {
        delay(2000)
        val prompt = "Create 3 educational flashcards for the topic: $topic. Each should have a question and an answer."
        val response = """
            1. Q: What is the primary purpose of $topic?
               A: To optimize data processing and storage in complex systems.
            
            2. Q: Name a key characteristic of $topic.
               A: Scalability and efficiency are its core traits.
            
            3. Q: How does $topic benefit developers?
               A: It provides a standardized way to solve common structural problems.
        """.trimIndent()
        return Result.success(LLMResponse(prompt, response))
    }
}
