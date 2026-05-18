package com.example.llmlearningassistant.data.repository

import com.example.llmlearningassistant.data.model.LLMResponse
import com.example.llmlearningassistant.data.remote.ChatMessage
import com.example.llmlearningassistant.data.remote.ChatRequest
import com.example.llmlearningassistant.data.remote.LLMApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LLMRepositoryImpl(
    private val apiService: LLMApiService,
    private val apiKey: String
) : LLMRepository {

    private val model = "gpt-3.5-turbo"

    override suspend fun explainAnswer(
        question: String,
        correctAnswer: String,
        userAnswer: String
    ): Result<LLMResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Explain why '$userAnswer' is ${if (correctAnswer.equals(userAnswer, ignoreCase = true)) "correct" else "incorrect"} for the question: '$question'. The correct answer is '$correctAnswer'."
            
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", "You are a helpful learning assistant."),
                    ChatMessage("user", prompt)
                )
            )

            val response = apiService.getCompletion("Bearer $apiKey", request)
            val content = response.choices.firstOrNull()?.message?.content ?: "No response from AI"
            
            Result.success(LLMResponse(prompt, content))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateFlashcards(topic: String): Result<LLMResponse> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Create 3 educational flashcards for the topic: $topic. Each should have a question and an answer."
            
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", "You are a helpful learning assistant."),
                    ChatMessage("user", prompt)
                )
            )

            val response = apiService.getCompletion("Bearer $apiKey", request)
            val content = response.choices.firstOrNull()?.message?.content ?: "No response from AI"
            
            Result.success(LLMResponse(prompt, content))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
