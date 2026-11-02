package com.zayaai.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class PromptResult(
    val basicPrompt: String = "",
    val advancedPrompt: String = "",
    val proPrompt: String = "",
    val enhancementTips: String = ""
)

class PromptEngineerViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var result by mutableStateOf(PromptResult())
        private set

    var errorMessage by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf("General")
        private set

    val categories = listOf(
        "General", "Image Generation", "Text / Writing",
        "Code Generation", "Video / Animation", "Marketing / Ads",
        "Education", "Business", "Social Media"
    )

    fun setCategory(category: String) {
        selectedCategory = category
    }

    fun generatePrompts(userGoal: String) {
        if (userGoal.isBlank()) return

        isLoading = true
        result = PromptResult()
        errorMessage = ""

        viewModelScope.launch {
            try {
                val systemPrompt = buildString {
                    append("You are an expert AI Prompt Engineer. Generate highly effective, optimized prompts.\n\n")
                    append("RULES:\n")
                    append("- Category: $selectedCategory\n")
                    append("- Generate exactly 4 sections separated by these EXACT headers:\n")
                    append("  === BASIC PROMPT ===\n")
                    append("  === ADVANCED PROMPT ===\n")
                    append("  === PRO PROMPT ===\n")
                    append("  === ENHANCEMENT TIPS ===\n\n")
                    append("FOR EACH PROMPT LEVEL:\n")
                    append("- Basic: Simple, short, directly usable (2-3 sentences)\n")
                    append("- Advanced: Detailed with style, tone, format, constraints (1 paragraph)\n")
                    append("- Pro: Highly refined, structured, with technical details, role assignment, output format, and specific constraints (multi-paragraph)\n\n")

                    if (selectedCategory == "Image Generation") {
                        append("IMAGE PROMPT SPECIFICS  —  Include in Advanced & Pro:\n")
                        append("- Subject description, art style (realistic/cinematic/anime/3D)\n")
                        append("- Lighting, camera angle, lens type\n")
                        append("- Colors, mood, atmosphere\n")
                        append("- Background details\n")
                        append("- Quality tags: 4K, ultra-detailed, photorealistic, masterpiece\n\n")
                    } else if (selectedCategory == "Text / Writing" || selectedCategory == "Marketing / Ads") {
                        append("TEXT PROMPT SPECIFICS  —  Include in Advanced & Pro:\n")
                        append("- Role assignment (e.g., 'Act as an expert...')\n")
                        append("- Output format and structure\n")
                        append("- Tone, voice, and style instructions\n")
                        append("- Word count / length constraints\n")
                        append("- Target audience\n\n")
                    } else if (selectedCategory == "Code Generation") {
                        append("CODE PROMPT SPECIFICS  —  Include in Advanced & Pro:\n")
                        append("- Programming language and framework\n")
                        append("- Architecture pattern\n")
                        append("- Error handling requirements\n")
                        append("- Performance constraints\n")
                        append("- Code documentation requirements\n\n")
                    }

                    append("FOR ENHANCEMENT TIPS:\n")
                    append("- List exactly 5 numbered tips to further customize or improve the prompts\n")
                    append("- Make tips actionable and specific\n\n")
                    append("Make every prompt COPY-PASTE READY. No meta-commentary. No markdown formatting like ** or ##. Just clean text.")
                }

                val aiResponse = AIService.generate(
                    prompt = "Generate optimized AI prompts for this goal: $userGoal",
                    systemPrompt = systemPrompt
                )

                result = parseResponse(aiResponse)

                if (result.basicPrompt.isBlank() && result.advancedPrompt.isBlank() && result.proPrompt.isBlank()) {
                    // Fallback: treat entire response as the pro prompt
                    result = PromptResult(
                        basicPrompt = "",
                        advancedPrompt = "",
                        proPrompt = aiResponse.trim(),
                        enhancementTips = ""
                    )
                }

            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to generate prompts."
            } finally {
                isLoading = false
            }
        }
    }

    fun optimizePrompt(existingPrompt: String) {
        if (existingPrompt.isBlank()) return

        isLoading = true
        result = PromptResult()
        errorMessage = ""

        viewModelScope.launch {
            try {
                val systemPrompt = buildString {
                    append("You are a Prompt Optimization Expert.\n\n")
                    append("TASK: Improve the given prompt to make it more effective, detailed, and AI-friendly.\n\n")
                    append("RULES:\n")
                    append("- Keep the original intent\n")
                    append("- Add missing details (style, clarity, constraints)\n")
                    append("- Improve structure and wording\n")
                    append("- Make it more specific and powerful\n\n")
                    append("OUTPUT FORMAT  —  Use these EXACT section headers:\n")
                    append("  === BASIC PROMPT ===\n")
                    append("  (The original prompt cleaned up and clarified)\n")
                    append("  === ADVANCED PROMPT ===\n")
                    append("  (Significantly improved version with details)\n")
                    append("  === PRO PROMPT ===\n")
                    append("  (Highly optimized, structured, professional version)\n")
                    append("  === ENHANCEMENT TIPS ===\n")
                    append("  (5 numbered tips explaining what was improved and how to customize further)\n\n")
                    append("Make every prompt COPY-PASTE READY. No markdown formatting like ** or ##. Just clean text.")
                }

                val aiResponse = AIService.generate(
                    prompt = "Optimize this prompt: $existingPrompt",
                    systemPrompt = systemPrompt
                )

                result = parseResponse(aiResponse)

                if (result.basicPrompt.isBlank() && result.advancedPrompt.isBlank() && result.proPrompt.isBlank()) {
                    result = PromptResult(
                        basicPrompt = "",
                        advancedPrompt = "",
                        proPrompt = aiResponse.trim(),
                        enhancementTips = ""
                    )
                }

            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to optimize prompt."
            } finally {
                isLoading = false
            }
        }
    }

    private fun parseResponse(response: String): PromptResult {
        val basic = extractSection(response, "=== BASIC PROMPT ===", "=== ADVANCED PROMPT ===")
        val advanced = extractSection(response, "=== ADVANCED PROMPT ===", "=== PRO PROMPT ===")
        val pro = extractSection(response, "=== PRO PROMPT ===", "=== ENHANCEMENT TIPS ===")
        val tips = extractSection(response, "=== ENHANCEMENT TIPS ===", null)

        return PromptResult(
            basicPrompt = basic.trim(),
            advancedPrompt = advanced.trim(),
            proPrompt = pro.trim(),
            enhancementTips = tips.trim()
        )
    }

    private fun extractSection(text: String, startMarker: String, endMarker: String?): String {
        val startIndex = text.indexOf(startMarker, ignoreCase = true)
        if (startIndex == -1) return ""

        val contentStart = startIndex + startMarker.length
        val endIndex = if (endMarker != null) {
            val idx = text.indexOf(endMarker, startIndex = contentStart, ignoreCase = true)
            if (idx == -1) text.length else idx
        } else {
            text.length
        }

        return text.substring(contentStart, endIndex).trim()
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Prompt") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun clear() {
        result = PromptResult()
        errorMessage = ""
    }
}


