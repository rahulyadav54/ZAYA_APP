package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class QuizGeneratorViewModel : ViewModel() {
    var generatedQuiz by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    fun generateQuiz(topic: String) {
        if (topic.isBlank()) return
        
        isLoading = true
        generatedQuiz = ""
        viewModelScope.launch {
            try {
                generatedQuiz = AIService.generate(
                    prompt = "Create a challenging 5-question multiple choice quiz about: $topic. For each question provide 4 options (A, B, C, D) and mark the correct answer clearly.",
                    systemPrompt = "You are an expert quiz creator. Generate well-structured educational quizzes."
                )
            } catch (e: Exception) {
                generatedQuiz = "Error: ${e.localizedMessage ?: "Failed to generate quiz."}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun clear() {
        generatedQuiz = ""
    }
}

