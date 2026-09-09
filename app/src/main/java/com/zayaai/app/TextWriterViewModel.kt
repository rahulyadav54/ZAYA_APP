package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TextWriterViewModel : ViewModel() {
    var generatedText by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    fun generateText(prompt: String) {
        if (prompt.isBlank()) return
        
        isLoading = true
        generatedText = ""
        viewModelScope.launch {
            try {
                generatedText = AIService.generate(
                    prompt = prompt,
                    systemPrompt = "You are a professional copywriter and content creator. Write high-quality, engaging content using markdown formatting: **bold** for emphasis, ## headings for sections, bullet points for lists. Keep it concise and well-structured."
                )
            } catch (e: Exception) {
                generatedText = "Error: ${e.localizedMessage ?: "Failed to generate text."}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun clear() {
        generatedText = ""
    }
}

