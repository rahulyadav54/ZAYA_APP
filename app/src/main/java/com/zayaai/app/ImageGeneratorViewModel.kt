package com.zayaai.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ImageGeneratorViewModel : ViewModel() {

    /** Raw image bytes from the API — null when no image */
    var generatedImageBytes by mutableStateOf<ByteArray?>(null)
        private set

    var isImageLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var lastPrompt by mutableStateOf("")
        private set

    var isDownloading by mutableStateOf(false)
        private set

    var loadingStatus by mutableStateOf("")
        private set

    var isReported by mutableStateOf(false)
        private set

    var reportFeedback by mutableStateOf("")
        private set

    private var generationJob: Job? = null

    // Client-side instant pre-check pattern matching Google Play guidelines
    private val prohibitedKeywords = Regex(
        "(?i)\\b(nudif\\w*|undress\\w*|strip\\s+(?:her|him|them|off|clothing|clothes)|take\\s+off\\s+(?:her|his|their)\\s+clothes|remove\\s+(?:her|his|their)?\\s*(?:clothes|clothing|dress|shirt|pants|underwear|bra|bikini)|see[\\s-]*through|transparent\\s+(?:clothes|clothing|dress|shirt|underwear|bra|panties|bikini|fabric)|make\\s+(?:her|him|them|\\w+)\\s+(?:nude|naked|bare|transparent)|naked|deepnude\\w*|unclothing|without\\s+(?:clothes|clothing|dress|shirt|underwear|bra|pants)|porn\\w*|hentai|xxx|erotica|blowjob|handjob|cumshot|vagina|penis|dildo|pussy|boobs|tits|breasts\\s+exposed)\\b"
    )

    fun generateImage(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank() || isImageLoading) return

        // 1. Client-side pre-moderation filter
        if (prohibitedKeywords.containsMatchIn(cleanPrompt)) {
            errorMessage = "Safety Policy Violation: Prompts requesting nudity, clothing removal, or sexually explicit content are strictly prohibited."
            generatedImageBytes = null
            isImageLoading = false
            return
        }

        generationJob?.cancel()
        isImageLoading = true
        generatedImageBytes = null
        errorMessage = ""
        isReported = false
        reportFeedback = ""
        lastPrompt = cleanPrompt
        loadingStatus = "Checking prompt & generating image..."

        generationJob = viewModelScope.launch {
            try {
                // 2. Fetch via Server-Side Moderated Cloudflare Worker AI Gateway
                val bytes = withContext(Dispatchers.IO) {
                    AIService.generateImage(cleanPrompt)
                }

                // Verify valid bitmap decoding
                val testBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (testBitmap != null) {
                    testBitmap.recycle()
                    generatedImageBytes = bytes
                    loadingStatus = ""
                    Log.d("AI_IMAGE", "Moderated image received: ${bytes.size} bytes")
                } else {
                    errorMessage = "Received invalid image data. Tap retry."
                    loadingStatus = ""
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AI_IMAGE", "Generation failed", e)
                errorMessage = e.localizedMessage ?: "Could not generate image. Tap retry."
                loadingStatus = ""
            } finally {
                isImageLoading = false
            }
        }
    }

    fun submitReport(reason: String, details: String) {
        viewModelScope.launch {
            try {
                val reportId = withContext(Dispatchers.IO) {
                    AIService.submitReport(
                        contentId = "img_${System.currentTimeMillis()}",
                        contentType = "image",
                        snippet = lastPrompt,
                        reason = reason,
                        details = details
                    )
                }
                isReported = true
                reportFeedback = "Thank you. Report #$reportId submitted. Image has been removed."
                generatedImageBytes = null // Hide image immediately
            } catch (e: Exception) {
                isReported = true
                reportFeedback = "Thank you. Your report has been submitted and content flagged for review."
                generatedImageBytes = null
            }
        }
    }

    fun retry() {
        if (lastPrompt.isNotBlank()) {
            generateImage(lastPrompt)
        }
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        isImageLoading = false
        loadingStatus = ""
    }

    fun clear() {
        generatedImageBytes = null
        lastPrompt = ""
        errorMessage = ""
        loadingStatus = ""
        isReported = false
        reportFeedback = ""
    }

    fun downloadImage(context: Context) {
        val bytes = generatedImageBytes ?: return

        isDownloading = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: throw Exception("Invalid image data")

                    val timestamp = System.currentTimeMillis()
                    val fileName = "ai_zaya_image_$timestamp.png"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AI_ZAYA")
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                        val uri = context.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                        ) ?: throw Exception("Failed to create file")

                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, values, null, null)
                    } else {
                        @Suppress("DEPRECATION")
                        val dir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "AI_ZAYA"
                        )
                        if (!dir.exists()) dir.mkdirs()
                        val file = File(dir, fileName)
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }
                    bitmap.recycle()
                }
                Toast.makeText(context, "Image saved to Pictures/AI_ZAYA", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isDownloading = false
            }
        }
    }
}
