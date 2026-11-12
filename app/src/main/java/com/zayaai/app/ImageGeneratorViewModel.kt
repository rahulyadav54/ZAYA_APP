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
        // Feature is temporarily deactivated across the application
        isImageLoading = false
        generatedImageBytes = null
        errorMessage = "Image creation is temporarily unavailable while safety systems are upgraded."
    }

    fun submitReport(reason: String, details: String) {
        // In-app reporting retained for user safety
    }

    fun retry() {
        // Disabled
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
