package com.zayaai.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class VoiceState { IDLE, LISTENING, PROCESSING, SPEAKING }

class VoiceViewModel(context: Context, private val repository: AiZayaRepository) : ViewModel() {
    private val speechRecognizerManager = SpeechRecognizerManager(context)
    val ttsManager = TextToSpeechManager(context)

    val isListening: StateFlow<Boolean> = speechRecognizerManager.isListening
    val recognizedText: StateFlow<String> = speechRecognizerManager.recognizedText
    val rmsLevel: StateFlow<Float> = speechRecognizerManager.rmsLevel
    val partialText: StateFlow<String> = speechRecognizerManager.partialText
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    var aiResponse by mutableStateOf("")
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var voiceState by mutableStateOf(VoiceState.IDLE)
        private set

    // Continuous mode  —  auto-listen after AI finishes speaking
    var isContinuousMode by mutableStateOf(false)
        private set

    // Teacher mode  —  AI responds like an educator
    var isTeacherMode by mutableStateOf(false)
        private set

    // Conversation history for display
    private val _conversationHistory = mutableStateListOf<Pair<String, String>>()
    val conversationHistory: List<Pair<String, String>> = _conversationHistory

    // Context memory for AI
    private val _contextMessages = mutableListOf<OpenRouterMessage>()

    // Auto-listen job (cancellable for interruption)
    private var autoListenJob: Job? = null

    // Retry counter for no-speech-detected
    private var noSpeechRetryCount = 0
    private val maxNoSpeechRetries = 3

    private val normalPrompt = """You are AI ZAYA  —  a brilliant, friendly, and deeply knowledgeable AI voice assistant created by ZAYA CODE HUB. You know everything  —  science, math, history, technology, coding, current affairs, philosophy, health, business, arts, and more, just like the best AI assistants in the world.

PERSONALITY & VOICE RULES:
 •  You are like a smart, cheerful Indian girl talking to a friend  —  warm, natural, relatable.
 •  Speak in simple, everyday conversational English. Use short sentences that flow naturally when read aloud.
 •  NEVER use markdown, bullet points, numbered lists, asterisks, hashtags, or code blocks. Your words will be spoken by a voice engine  —  write exactly how a person would speak.
 •  Keep responses SHORT and punchy  —  2 to 5 sentences unless the user asks for more detail.
 •  Use natural filler words occasionally like "So basically", "Hmm, good question", "Right, so", "Okay so", "Well actually" to sound human.
 •  When explaining complex topics, use everyday analogies and examples.
 •  Be witty, warm, and occasionally add light humor.
 •  If you don't know something, say so honestly  —  don't make things up.
 •  Remember the full conversation and refer back to earlier topics naturally.

IDENTITY:
 •  You were created by ZAYA CODE HUB. CEO and Founder is Rahul Kumar Yadav, Co-Founder is Shivshankar Jaysawal, HR Manager is Akash Adhikari.
 •  NEVER say you are made by Google, OpenAI, Meta, or anyone else.
 •  Your name is AI ZAYA."""

    private val teacherPrompt = """You are AI ZAYA  —  a patient, encouraging, and expert AI tutor created by ZAYA CODE HUB. You teach like the best human teachers do, but you also know everything about the world.

TEACHING STYLE:
 •  Explain step-by-step, clearly and simply  —  like talking to someone hearing it for the first time.
 •  Use analogies and real-world examples to make concepts click.
 •  After explaining, ask a follow-up like "Does that make sense?" or "Want me to go deeper?"
 •  If the student is confused, try a completely different approach.
 •  Be warm  —  say things like "Great question!", "You're on the right track!", "That's a smart observation!"

VOICE RULES:
 •  Speak in natural conversational English, like a friendly Indian girl explaining things.
 •  NEVER use markdown, bullet points, code blocks, or special formatting. Write exactly as a person would speak.
 •  Keep answers SHORT  —  2 to 5 sentences for voice delivery. Expand only if asked.
 •  Use natural transitions like "So basically", "Right, so what happens is", "Think of it like this".

IDENTITY:
 •  Created by ZAYA CODE HUB. CEO and Founder: Rahul Kumar Yadav, Co-Founder: Shivshankar Jaysawal, HR Manager: Akash Adhikari.
 •  Never mention Google, OpenAI, or any other company as your creator."""

    private val systemPrompt: String
        get() = if (isTeacherMode) teacherPrompt else normalPrompt

    /**
     * Check if the user message is about ZAYA CODE HUB team members and return
     * a hardcoded response. Returns null if the message is not about the team.
     */
    private fun getZayaTeamResponse(text: String): String? {
        val lower = text.lowercase()
        val isAboutZaya = lower.contains("zaya") || lower.contains("company") ||
                lower.contains("who made") || lower.contains("who created") ||
                lower.contains("who developed") || lower.contains("your team") ||
                lower.contains("your company")

        return when {
            isAboutZaya && (lower.contains("ceo") || lower.contains("founder") && !lower.contains("co-founder") && !lower.contains("cofounder") ||
                    lower.contains("leader") || lower.contains("owner")) ->
                "The CEO and Founder of ZAYA CODE HUB is Rahul Kumar Yadav. He leads the vision and strategy of the company."

            isAboutZaya && (lower.contains("co-founder") || lower.contains("cofounder") || lower.contains("co founder")) ->
                "The Co-Founder of ZAYA CODE HUB is Shivshankar Jaysawal. He plays a key role in building and growing the company alongside the founder."

            isAboutZaya && (lower.contains("hr") || lower.contains("human resource") || lower.contains("hr manager")) ->
                "The HR Manager of ZAYA CODE HUB is Akash Adhikari. He manages the human resources and team operations."

            isAboutZaya && (lower.contains("team") || lower.contains("members") || lower.contains("people behind")) ->
                "The ZAYA CODE HUB leadership team includes: Rahul Kumar Yadav as CEO and Founder, Shivshankar Jaysawal as Co-Founder, and Akash Adhikari as HR Manager. Together, they drive the vision, development, and operations of ZAYA CODE HUB."

            else -> null
        }
    }

    val contextCount: Int get() = _contextMessages.size

    fun toggleContinuousMode() {
        isContinuousMode = !isContinuousMode
    }

    fun toggleTeacherMode() {
        isTeacherMode = !isTeacherMode
    }

    // ─── START LISTENING ───

    fun startListening() {
        autoListenJob?.cancel()
        noSpeechRetryCount = 0
        if (ttsManager.isSpeaking.value) {
            // Interruption: user taps mic while AI is speaking
            ttsManager.stop()
            viewModelScope.launch {
                delay(250)
                doStartListening()
            }
        } else {
            doStartListening()
        }
    }

    private fun doStartListening() {
        aiResponse = ""
        voiceState = VoiceState.LISTENING
        speechRecognizerManager.startListening { text ->
            if (text.isNotBlank() && text != "Listening..." && !text.startsWith("Error") && !text.startsWith("No speech")) {
                noSpeechRetryCount = 0 // Reset on successful speech
                sendToAi(text)
            } else {
                // No speech detected  —  auto-retry up to 3 times
                noSpeechRetryCount++
                if (noSpeechRetryCount < maxNoSpeechRetries) {
                    scheduleAutoListen(600)
                } else {
                    // Exhausted retries  —  go idle
                    noSpeechRetryCount = 0
                    voiceState = VoiceState.IDLE
                }
            }
        }
    }

    // ─── INTERRUPTION: User speaks while AI is talking ───

    fun interruptAndListen() {
        autoListenJob?.cancel()
        ttsManager.stop()
        voiceState = VoiceState.IDLE
        viewModelScope.launch {
            delay(200)
            doStartListening()
        }
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
        voiceState = VoiceState.IDLE
    }

    fun stopSpeaking() {
        autoListenJob?.cancel()
        ttsManager.stop()
        voiceState = VoiceState.IDLE
    }

    fun clearConversation() {
        autoListenJob?.cancel()
        _conversationHistory.clear()
        _contextMessages.clear()
        aiResponse = ""
        voiceState = VoiceState.IDLE
    }

    // ─── SEND TO AI ───

    private fun sendToAi(text: String) {
        isProcessing = true
        voiceState = VoiceState.PROCESSING
        viewModelScope.launch {
            try {
                _contextMessages.add(OpenRouterMessage("user", text))

                val teamResponse = getZayaTeamResponse(text)
                val textResponse = if (teamResponse != null) {
                    teamResponse
                } else {
                    val fullContext = buildList {
                        add(OpenRouterMessage("system", systemPrompt))
                        addAll(_contextMessages.takeLast(15))
                    }
                    AIService.chat(fullContext)
                }

                // Strip markdown for cleaner TTS
                val spokenText = stripMarkdownForSpeech(textResponse)

                _contextMessages.add(OpenRouterMessage("assistant", textResponse))

                aiResponse = textResponse
                _conversationHistory.add(text to textResponse)
                voiceState = VoiceState.SPEAKING

                // Small natural delay before speaking (like a real teacher pausing)
                delay(300)

                ttsManager.speak(spokenText) {
                    voiceState = VoiceState.IDLE
                    if (isContinuousMode) {
                        scheduleAutoListen(1200)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("connect", ignoreCase = true) == true ->
                        "I'm having trouble connecting. Please check your internet."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "That took too long. Could you repeat your question?"
                    else ->
                        "Sorry, something went wrong. Could you try asking again?"
                }
                aiResponse = errorMsg
                _conversationHistory.add(text to errorMsg)
                voiceState = VoiceState.SPEAKING
                ttsManager.speak(errorMsg) {
                    voiceState = VoiceState.IDLE
                }
            } finally {
                isProcessing = false
            }
        }
    }

    // ─── AUTO-LISTEN SCHEDULING ───

    private fun scheduleAutoListen(delayMs: Long) {
        autoListenJob?.cancel()
        autoListenJob = viewModelScope.launch {
            delay(delayMs)
            if (voiceState == VoiceState.IDLE || voiceState == VoiceState.LISTENING) {
                doStartListening()
            }
        }
    }

    // ─── Strip markdown and clean for natural TTS ───

    private fun stripMarkdownForSpeech(text: String): String {
        return text
            .replace(Regex("```[\\s\\S]*?```"), "")                    // Remove code blocks entirely
            .replace(Regex("#{1,6}\\s*"), "")                          // Remove headings
            .replace(Regex("\\*{1,3}([^*]+)\\*{1,3}"), "$1")          // Remove bold/italic
            .replace(Regex("_{1,2}([^_]+)_{1,2}"), "$1")              // Remove underline emphasis
            .replace(Regex("`([^`]+)`"), "$1")                         // Remove inline code
            .replace(Regex("\\[([^]]+)]\\([^)]+\\)"), "$1")           // Links → text only
            .replace(Regex("^[ • â—â–ª\\-\\*]\\s+", RegexOption.MULTILINE), "") // Remove bullets
            .replace(Regex("^\\d+\\.\\s+", RegexOption.MULTILINE), "") // Remove numbered lists
            .replace(Regex("\\|[^|\\n]*\\|"), "")                      // Remove table rows
            .replace(Regex("---+"), "")                                // Remove horizontal rules
            .replace(Regex(">[\\s]*"), "")                             // Remove blockquotes
            .replace(Regex("!\\[.*?]\\(.*?\\)"), "")                  // Remove images
            .trim()
    }

    override fun onCleared() {
        super.onCleared()
        autoListenJob?.cancel()
        speechRecognizerManager.destroy()
        ttsManager.destroy()
    }
}

