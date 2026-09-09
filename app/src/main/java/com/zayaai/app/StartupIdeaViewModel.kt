package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StartupIdeaViewModel : ViewModel() {
    var generatedIdea by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun generateIdea(industry: String, problem: String, extras: String) {
        if (industry.isBlank() && problem.isBlank()) return

        isLoading = true
        generatedIdea = ""

        val prompt = buildString {
            append("Generate a detailed and innovative startup idea")
            if (industry.isNotBlank()) append(" in the $industry industry")
            if (problem.isNotBlank()) append(" that solves: $problem")
            if (extras.isNotBlank()) append(". Additional context: $extras")
            append(".")
            append("\n\nPlease provide:\n")
            append("🚀 **Startup Name** — A catchy, memorable name\n")
            append("💡 **One-Line Pitch** — Elevator pitch in one sentence\n")
            append("🎯 **Problem** — The specific problem being solved\n")
            append("✅ **Solution** — How the startup solves it\n")
            append("👥 **Target Audience** — Who are the users/customers\n")
            append("💰 **Revenue Model** — How it makes money\n")
            append("🏆 **Competitive Advantage** — What makes it unique\n")
            append("📊 **Market Size** — Estimated market opportunity\n")
            append("🛠️ **MVP Features** — Top 5 features for minimum viable product\n")
            append("📈 **Growth Strategy** — How to scale\n")
            append("⚠️ **Key Risks** — Main challenges and how to mitigate them\n")
            append("💵 **Estimated Startup Cost** — Initial investment needed\n")
        }

        viewModelScope.launch {
            try {
                generatedIdea = AIService.generate(
                    prompt = prompt,
                    systemPrompt = "You are a world-class startup advisor and venture capitalist with deep expertise in technology, business models, and market analysis. Generate creative, feasible, and detailed startup ideas. Use clear formatting with emojis and bold headers. Be specific with numbers and actionable advice."
                )
            } catch (e: Exception) {
                generatedIdea = "Error: ${e.localizedMessage ?: "Failed to generate startup idea."}"
            } finally {
                isLoading = false
            }
        }
    }

    fun generateRandom() {
        isLoading = true
        generatedIdea = ""

        val industries = listOf(
            "AI/Machine Learning", "Healthcare", "Education", "FinTech",
            "Sustainability", "Food Tech", "Social Media", "E-commerce",
            "Cybersecurity", "Remote Work", "Gaming", "Real Estate",
            "Transportation", "Agriculture", "Mental Health", "IoT"
        )

        val prompt = """
            Generate a completely unique and innovative startup idea in the ${industries.random()} space.
            Make it creative, feasible, and something that could realistically be built today.
            
            Please provide:
            🚀 **Startup Name** — A catchy, memorable name
            💡 **One-Line Pitch** — Elevator pitch in one sentence
            🎯 **Problem** — The specific problem being solved
            ✅ **Solution** — How the startup solves it
            👥 **Target Audience** — Who are the users/customers
            💰 **Revenue Model** — How it makes money
            🏆 **Competitive Advantage** — What makes it unique
            📊 **Market Size** — Estimated market opportunity
            🛠️ **MVP Features** — Top 5 features for minimum viable product
            📈 **Growth Strategy** — How to scale
            ⚠️ **Key Risks** — Main challenges and how to mitigate them
            💵 **Estimated Startup Cost** — Initial investment needed
        """.trimIndent()

        viewModelScope.launch {
            try {
                generatedIdea = AIService.generate(
                    prompt = prompt,
                    systemPrompt = "You are a world-class startup advisor and venture capitalist. Generate creative, feasible, and detailed startup ideas. Use clear formatting with emojis and bold headers. Be specific and actionable."
                )
            } catch (e: Exception) {
                generatedIdea = "Error: ${e.localizedMessage ?: "Failed to generate startup idea."}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clear() {
        generatedIdea = ""
    }
}


