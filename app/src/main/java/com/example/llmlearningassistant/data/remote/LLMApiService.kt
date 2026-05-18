package com.example.llmlearningassistant.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class ChatMessage(val role: String, val content: String)
data class ChatRequest(val model: String, val messages: List<ChatMessage>)
data class ChatChoice(val message: ChatMessage)
data class ChatResponse(val choices: List<ChatChoice>)

interface LLMApiService {
    @POST("v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse
}
