package com.zayaai.app

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private var onSpeechDone: (() -> Unit)? = null

    // Default locale — Indian English for a natural Indian accent
    private val indianEnglish = Locale("en", "IN")

    init {
        initTts(context)
    }

    private fun initTts(ctx: Context) {
        val googleTtsPackage = "com.google.android.tts"
        try {
            // Attempt to bind Google Speech Services directly for neural/wavenet human voice synthesis
            tts = TextToSpeech(ctx, { status ->
                if (status == TextToSpeech.SUCCESS) {
                    configureHumanizedTts()
                } else {
                    initFallbackTts(ctx)
                }
            }, googleTtsPackage)
        } catch (_: Exception) {
            initFallbackTts(ctx)
        }
    }

    private fun initFallbackTts(ctx: Context) {
        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureHumanizedTts()
            }
        }
    }

    private fun configureHumanizedTts() {
        try {
            // 1. AudioAttributes for assistant voice routing and acoustic clarity
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)

            // 2. Set Indian English locale by default
            val result = tts?.setLanguage(indianEnglish)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }

            // 3. Humanized acoustic parameters:
            // Pitch 1.0f produces natural, rich, warm vocal resonance (replaces metallic 1.15f)
            // Speech rate 1.0f produces fluent, conversational pacing (replaces dragging 0.92f)
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(1.0f)

            // 4. Select the most natural human neural voice
            selectBestHumanVoice(indianEnglish)

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
        } catch (e: Exception) {
            Log.w("TTS", "Failed to configure TTS: ${e.message}")
        }
    }

    /**
     * Select the most natural, human-sounding neural voice available.
     * Evaluates Google Neural / WaveNet voices with high quality and latency profiles.
     */
    private fun selectBestHumanVoice(targetLocale: Locale) {
        try {
            val allVoices = tts?.voices ?: return
            val targetLang = targetLocale.language
            val targetCountry = targetLocale.country

            val availableVoices = allVoices.filter { voice ->
                // Skip voices marked as not installed
                !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
            }

            val scored = availableVoices.map { voice ->
                var score = 0
                val name = voice.name.lowercase()
                val lang = voice.locale.language
                val country = voice.locale.country

                // 1. Language match
                if (lang == targetLang) {
                    score += 100
                    if (targetCountry.isNotBlank() && country.equals(targetCountry, ignoreCase = true)) {
                        score += 50
                    }
                } else if (lang == "en") {
                    score += 40
                } else {
                    return@map voice to -1
                }

                // 2. High Quality & Very High Quality neural weights
                if (voice.quality >= Voice.QUALITY_VERY_HIGH) score += 90
                else if (voice.quality >= Voice.QUALITY_HIGH) score += 60
                else if (voice.quality >= Voice.QUALITY_NORMAL) score += 20

                // 3. Google Neural / WaveNet signature identifiers
                if (name.contains("wavenet") || name.contains("neural") || name.contains("journey")) {
                    score += 100
                }
                if (name.contains("-x-")) {
                    score += 60 // Google modern voice architecture
                }
                if (name.contains("network")) {
                    score += 40 // Online Google neural synthesis is studio-grade human
                }

                // 4. Feminine / Warm conversational timbre
                if (name.contains("female") || name.contains("#female") || name.contains("woman")) score += 40
                if (name.contains("end") || name.contains("ene") || name.contains("cxx") || name.contains("enc")) {
                    score += 60 // Premier Indian English neural voices
                }
                if (name.contains("aditi") || name.contains("raveena") || name.contains("priya") ||
                    name.contains("neerja") || name.contains("sapna") || name.contains("lekha")) {
                    score += 70
                }

                voice to score
            }.filter { it.second > 0 }.sortedByDescending { it.second }

            val best = scored.firstOrNull()?.first
            if (best != null) {
                tts?.voice = best
                Log.d("TTS", "Selected humanized voice: ${best.name} (quality: ${best.quality}, locale: ${best.locale})")
            }
        } catch (e: Exception) {
            Log.w("TTS", "Voice selection exception: ${e.message}")
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) return
        onSpeechDone = onComplete

        // Auto-detect language: Devanagari → Hindi, otherwise Indian English
        val isHindi = text.any { it in '\u0900'..'\u097F' }
        val targetLocale = if (isHindi) Locale("hi", "IN") else indianEnglish

        try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            } else {
                selectBestHumanVoice(targetLocale)
            }
        } catch (_: Exception) {}

        // Natural conversational speech harmonics
        tts?.setPitch(1.0f)
        tts?.setSpeechRate(1.0f)

        // Clean text with humanized prosody & breathing pauses
        val cleanedText = cleanForSpeech(text)

        val params = Bundle()
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Clean text so it sounds natural, smooth, and human-like when spoken aloud.
     * Expands technical shorthand, inserts breathing pauses, and strips raw syntax.
     */
    private fun cleanForSpeech(text: String): String {
        var t = text

        // 1. Remove code blocks, markdown tables, URLs, and image tags completely
        t = t.replace(Regex("```[\\s\\S]*?```"), "")
        t = t.replace(Regex("`[^`]+`"), "")
        t = t.replace(Regex("https?://\\S+"), "")
        t = t.replace(Regex("!\\[[^\\]]*\\]\\([^)]*\\)"), "")

        // 2. Remove markdown formatting but preserve the words
        t = t.replace(Regex("#{1,6}\\s*"), "")
        t = t.replace(Regex("\\*{1,3}([^*]+)\\*{1,3}"), "$1")
        t = t.replace(Regex("_{1,2}([^_]+)_{1,2}"), "$1")
        t = t.replace(Regex("~~([^~]+)~~"), "$1")
        t = t.replace(Regex("\\[([^\\]]+)\\]\\([^)]*\\)"), "$1")

        // 3. Clean bullet points & lists into natural spoken pauses
        t = t.replace(Regex("(?m)^\\s*[-*•]\\s+"), "")
        t = t.replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "")

        // 4. Humanize abbreviations & technical terms
        t = t.replace(Regex("\\bAI\\b"), "A.I.")
        t = t.replace(Regex("\\bA\\.I\\b"), "A.I.")
        t = t.replace(Regex("\\bAPI\\b"), "A.P.I.")
        t = t.replace(Regex("\\bUI\\b"), "U.I.")
        t = t.replace(Regex("\\bUX\\b"), "U.X.")
        t = t.replace(Regex("\\be\\.g\\.\\s*"), "for example, ")
        t = t.replace(Regex("\\bi\\.e\\.\\s*"), "that is, ")
        t = t.replace(Regex("\\betc\\.\\s*"), "and so on. ")
        t = t.replace(Regex("\\bvs\\.?\\s*"), "versus ")
        t = t.replace(Regex("\\bapprox\\.?\\s*"), "approximately ")
        t = t.replace(Regex("\\bDr\\.\\s*"), "Doctor ")
        t = t.replace(Regex("\\bMr\\.\\s*"), "Mister ")
        t = t.replace(Regex("\\bMrs\\.\\s*"), "Missus ")
        t = t.replace(Regex("\\bProf\\.\\s*"), "Professor ")

        // 5. Currency & Units
        t = t.replace("₹", " rupees ")
        t = t.replace("$", " dollars ")
        t = t.replace("%", " percent ")
        t = t.replace("&", " and ")
        t = t.replace("@", " at ")
        t = t.replace("+", " plus ")
        t = t.replace("=", " equals ")
        t = t.replace("/", " or ")

        // 6. Conversational breathing pauses:
        // Turn hard colons and semicolons into soft comma pauses
        t = t.replace(":", ", ")
        t = t.replace(";", ", ")
        t = t.replace(" — ", ", ")
        t = t.replace(" – ", ", ")
        t = t.replace(" - ", ", ")
        t = t.replace("...", ", ")

        // Paragraph breaks become a clear sentence boundary
        t = t.replace(Regex("\\n{2,}"), ". ")
        t = t.replace("\n", " ")

        // Remove parenthesis and brackets without breaking sentence
        t = t.replace("(", ", ")
        t = t.replace(")", ", ")

        // Strip any remaining special symbols that sound robotic
        t = t.replace(Regex("[<>|~`#^]"), "")

        // Clean repeated punctuation & whitespace
        t = t.replace(Regex(",\\s*,"), ",")
        t = t.replace(Regex("\\.\\s*\\."), ".")
        t = t.replace(Regex(",\\s*\\."), ".")
        t = t.replace(Regex("\\s{2,}"), " ")

        return t.trim()
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
