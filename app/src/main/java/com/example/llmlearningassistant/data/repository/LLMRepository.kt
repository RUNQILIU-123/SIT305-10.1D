package com.example.llmlearningassistant.data.repository

import com.example.llmlearningassistant.data.model.ExplanationResponse
import com.example.llmlearningassistant.data.model.Flashcard
import com.example.llmlearningassistant.data.model.LLMResponse

interface LLMRepository {
    suspend fun explainAnswer(
        question: String,
        correctAnswer: String,
        userAnswer: String
    ): Result<LLMResponse>

    suspend fun generateFlashcards(topic: String): Result<LLMResponse>
}
