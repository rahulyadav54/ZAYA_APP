package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CodeGeneratorViewModel : ViewModel() {
    var generatedCode by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    fun generateCode(prompt: String) {
        if (prompt.isBlank()) return
        
        isLoading = true
        generatedCode = ""
        viewModelScope.launch {
            try {
                generatedCode = AIService.generate(
                    prompt = prompt,
                    systemPrompt = """You are an expert software engineer. Follow these rules strictly:
1. Always wrap code in markdown code blocks with the language tag (```kotlin, ```python, etc.)
2. Include brief inline comments explaining key logic
3. Keep explanations very short â€” max 2-3 sentences before or after code
4. Use bullet points for any explanation
5. Do NOT repeat the user's question
6. Produce clean, production-ready, well-structured code"""
                )
            } catch (e: Exception) {
                generatedCode = "Error: ${e.localizedMessage ?: "Failed to generate code."}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun clear() {
        generatedCode = ""
    }
}

