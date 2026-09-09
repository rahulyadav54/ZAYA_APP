package com.zayaai.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class SpeechRecognizerManager(context: Context) {

    companion object {
        private const val TAG = "SpeechRecognizerMgr"
    }

    // Use applicationContext to prevent Activity/Fragment memory leaks
    private val appContext = context.applicationContext
    private var speechRecognizer: SpeechRecognizer? = SpeechRecognizer.createSpeechRecognizer(appContext)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    private var onResult: ((String) -> Unit)? = null

    init {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _recognizedText.value = "Listening..."
                _rmsLevel.value = 0f
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize RMS level (0.0 to 1.0)
                _rmsLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                _rmsLevel.value = 0f
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _rmsLevel.value = 0f

                val errorMessage = getErrorMessage(error)
                Log.w(TAG, "Speech recognition error ($error): $errorMessage")

                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        // No speech detected â€” notify ViewModel so it can auto-retry
                        _recognizedText.value = "No speech detected"
                        onResult?.invoke("No speech detected")
                    }
                    SpeechRecognizer.ERROR_CLIENT -> {
                        // Usually caused by calling startListening while already active â€” safe to retry
                        _recognizedText.value = ""
                        onResult?.invoke("")
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        // Recognizer busy â€” stop first, then let ViewModel retry
                        speechRecognizer?.cancel()
                        _recognizedText.value = ""
                        onResult?.invoke("")
                    }
                    SpeechRecognizer.ERROR_AUDIO -> {
                        _recognizedText.value = "Audio recording error."
                        onResult?.invoke("Error: audio")
                    }
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        _recognizedText.value = "Network error."
                        onResult?.invoke("Error: network")
                    }
                    SpeechRecognizer.ERROR_SERVER -> {
                        _recognizedText.value = "Server error."
                        onResult?.invoke("Error: server")
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        _recognizedText.value = "Microphone permission not granted."
                        onResult?.invoke("Error: permission")
                    }
                    else -> {
                        // Unknown error â€” trigger callback so ViewModel doesn't get stuck
                        _recognizedText.value = errorMessage
                        onResult?.invoke("")
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val result = data?.firstOrNull() ?: ""
                _recognizedText.value = result
                _partialText.value = ""
                onResult?.invoke(result)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = data?.firstOrNull() ?: ""
                if (partial.isNotBlank()) {
                    _partialText.value = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening(callback: (String) -> Unit) {
        // Guard: if already listening, stop first to avoid ERROR_RECOGNIZER_BUSY
        if (_isListening.value) {
            speechRecognizer?.cancel()
            _isListening.value = false
        }

        onResult = callback
        _partialText.value = ""
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    /** Map error codes to human-readable descriptions for logging. */
    private fun getErrorMessage(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
        else -> "Unknown error ($errorCode)"
    }
}

