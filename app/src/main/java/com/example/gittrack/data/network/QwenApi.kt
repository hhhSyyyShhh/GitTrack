package com.example.gittrack.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface QwenApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}

data class ChatRequest(val model: String, val messages: List<ChatMessage>, val temperature: Double = 0.3)
data class ChatMessage(val role: String, val content: String)
data class ChatResponse(val choices: List<ChatChoice> = emptyList())
data class ChatChoice(val message: ChatMessage)
