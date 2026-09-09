package com.zayaai.app

object LocalAiManager {
    // Uses the centralized OpenRouter AIService
    suspend fun generateResponse(prompt: String): String? {
        return try {
            AIService.generate(prompt)
        } catch (e: Exception) {
            null
        }
    }
}

