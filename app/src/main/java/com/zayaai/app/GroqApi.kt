package com.zayaai.app

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class GroqRequest(
    val model: String = "llama3-8b-8192",
    val messages: List<GroqMessage>
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage
)

interface GroqApi {
    @POST("v1/chat/completions")
    suspend fun chat(
        @Header("Authorization") apiKey: String,
        @Body request: GroqRequest
    ): GroqResponse
}

