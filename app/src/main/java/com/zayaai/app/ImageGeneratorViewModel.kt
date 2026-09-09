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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ImageGeneratorViewModel : ViewModel() {

    /** Raw image bytes from the API â€” null when no image */
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

    private var generationJob: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    fun generateImage(prompt: String) {
        if (prompt.isBlank() || isImageLoading) return

        generationJob?.cancel()
        isImageLoading = true
        generatedImageBytes = null
        errorMessage = ""
        lastPrompt = prompt
        loadingStatus = "Generating image..."

        generationJob = viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    fetchImageBytes(prompt)
                }

                if (bytes != null) {
                    // Verify it decodes to a valid bitmap
                    val testBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (testBitmap != null) {
                        testBitmap.recycle()
                        generatedImageBytes = bytes
                        loadingStatus = ""
                        Log.d("AI_IMAGE", "Image received: ${bytes.size} bytes")
                    } else {
                        errorMessage = "Received invalid image data. Tap retry."
                        loadingStatus = ""
                    }
                } else {
                    errorMessage = "Could not generate image. Tap retry."
                    loadingStatus = ""
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AI_IMAGE", "Generation failed", e)
                errorMessage = "Error: ${e.localizedMessage ?: "Unknown error"}. Tap retry."
                loadingStatus = ""
            } finally {
                isImageLoading = false
            }
        }
    }

    /**
     * Try multiple Pollinations URL formats to fetch image bytes.
     * Uses proper Uri.encode for path segments and browser User-Agent.
     */
    private fun fetchImageBytes(prompt: String): ByteArray? {
        val encodedPrompt = Uri.encode(prompt)
        val seed = (0..999999).random()
        val ts = System.currentTimeMillis()

        // Multiple URL variants â€” try fastest configs first
        val urls = listOf(
            // Default model (fastest available)
            "https://image.pollinations.ai/prompt/$encodedPrompt?width=512&height=512&nologo=true&seed=$seed&t=$ts",
            // Explicit turbo model
            "https://image.pollinations.ai/prompt/$encodedPrompt?width=512&height=512&nologo=true&model=turbo&seed=${seed+1}&t=${ts+1}",
            // Explicit flux model
            "https://image.pollinations.ai/prompt/$encodedPrompt?width=512&height=512&nologo=true&model=flux&seed=${seed+2}&t=${ts+2}"
        )

        for ((index, url) in urls.withIndex()) {
            try {
                Log.d("AI_IMAGE", "Attempt ${index+1}: $url")

                val request = Request.Builder()
                    .url(url)
                    // Use a standard browser User-Agent â€” some APIs block custom agents
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "image/*")
                    .build()

                val response = httpClient.newCall(request).execute()
                response.use { resp ->
                    Log.d("AI_IMAGE", "Response: HTTP ${resp.code}, Content-Type: ${resp.header("Content-Type")}")

                    if (resp.isSuccessful) {
                        val contentType = resp.header("Content-Type") ?: ""
                        val bytes = resp.body?.bytes()

                        if (bytes != null && bytes.size > 1000 &&
                            (contentType.contains("image") || contentType.contains("octet-stream"))) {
                            Log.d("AI_IMAGE", "Got image: ${bytes.size} bytes")
                            return bytes
                        } else {
                            Log.w("AI_IMAGE", "Bad response: size=${bytes?.size}, type=$contentType")
                        }
                    } else {
                        Log.w("AI_IMAGE", "HTTP error ${resp.code}")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("AI_IMAGE", "Attempt ${index+1} failed: ${e.message}")
            }
        }
        return null
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
                        val dir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "AI_ZAYA"
                        )
                        dir.mkdirs()
                        val file = File(dir, fileName)
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }

                    bitmap.recycle()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Image saved to Pictures/AI_ZAYA", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AI_IMAGE", "Download failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isDownloading = false
            }
        }
    }

    fun clear() {
        generationJob?.cancel()
        generatedImageBytes = null
        errorMessage = ""
        lastPrompt = ""
        isImageLoading = false
        loadingStatus = ""
    }
}

