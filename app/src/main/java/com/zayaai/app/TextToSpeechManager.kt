package com.zayaai.app

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private var onSpeechDone: (() -> Unit)? = null

    // Default locale  —  Indian English for a natural Indian accent
    private val indianEnglish = Locale("en", "IN")

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // 1. Set Indian English locale
                val result = tts?.setLanguage(indianEnglish)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to regular English if en_IN not available
                    tts?.language = Locale.US
                }

                // 2. Try to pick the best female voice
                selectBestFemaleVoice()

                // 3. Natural feminine voice: slightly higher pitch, relaxed pace
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(0.92f)

                isInitialized = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeechDone?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    /**
     * Attempt to select the best female / natural-sounding voice.
     * Priority: Indian English female → any English female → any Indian English → default.
     */
    private fun selectBestFemaleVoice() {
        try {
            val voices = tts?.voices ?: return

            // Score each voice  —  higher is better
            val scored = voices
                .filter { !it.isNetworkConnectionRequired || it.features.isEmpty() }
                .map { voice ->
                    var score = 0
                    val name = voice.name.lowercase()
                    val lang = voice.locale.language
                    val country = voice.locale.country

                    // Must be English
                    if (lang != "en") return@map voice to -1

                    // Prefer Indian English
                    if (country == "IN") score += 30

                    // Prefer female-sounding voices (heuristic: common keywords in voice names)
                    if (name.contains("female") || name.contains("woman")) score += 50
                    // Many TTS engines use letters  —  voices with higher letters (e.g. voice 2, 3) are often female
                    if (name.contains("#female") || name.contains("ishi") || name.contains("lekha") ||
                        name.contains("aditi") || name.contains("raveena") || name.contains("priya") ||
                        name.contains("neerja") || name.contains("sapna")) score += 60

                    // Google TTS high quality voices
                    if (name.contains("en-in") && (name.contains("c") || name.contains("d"))) score += 20
                    if (name.contains("en-in-language")) score += 15

                    // Prefer non-network voices for reliability
                    if (!voice.isNetworkConnectionRequired) score += 5

                    // Prefer higher quality
                    if (voice.quality >= Voice.QUALITY_HIGH) score += 10
                    if (voice.quality >= Voice.QUALITY_VERY_HIGH) score += 15

                    voice to score
                }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }

            val bestVoice = scored.firstOrNull()?.first
            if (bestVoice != null) {
                tts?.voice = bestVoice
            }
        } catch (_: Exception) {
            // If voice selection fails, the default voice is still fine
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) return
        onSpeechDone = onComplete

        // Auto-detect language: Devanagari → Hindi, otherwise Indian English
        val locale = if (text.any { it in '\u0900'..'\u097F' }) {
            Locale("hi", "IN")
        } else {
            indianEnglish
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.US
        }

        // Clean text for more natural speech
        val cleanedText = cleanForSpeech(text)

        val params = Bundle()
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Clean text so it sounds natural when spoken aloud.
     * Do NOT insert newlines  —  that causes choppy stop-start speech.
     * Let the TTS engine handle sentence pauses naturally.
     */
    private fun cleanForSpeech(text: String): String {
        return text
            // Replace newlines/paragraphs with spaces so TTS flows continuously
            .replace("\n\n", ". ")
            .replace("\n", " ")
            // Expand abbreviations to sound natural
            .replace("e.g.", "for example")
            .replace("i.e.", "that is")
            .replace("etc.", "and so on")
            .replace("vs.", "versus")
            .replace("Dr.", "Doctor")
            .replace("Mr.", "Mister")
            .replace("Mrs.", "Missus")
            .replace("Prof.", "Professor")
            // Replace symbols that sound awkward
            .replace("...", ", ")
            .replace(" — ", ", ")
            .replace(" - ", ", ")
            .replace("(", ", ")
            .replace(")", ", ")
            .replace("/", " or ")
            .replace("&", " and ")
            .replace("@", " at ")
            .replace("+", " plus ")
            .replace("=", " equals ")
            // Remove any leftover markdown or special chars
            .replace(Regex("[*_#`~>|]"), "")
            .replace(Regex("\\[([^]]+)]"), "$1")
            // Collapse multiple spaces
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        _isSpeaking.value = false
    }
}

