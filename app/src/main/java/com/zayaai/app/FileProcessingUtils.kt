package com.zayaai.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

/**
 * Utility functions for processing files (images, PDFs, text) for the AI Chat upload feature.
 */
object FileProcessingUtils {

    private var pdfBoxInitialized = false

    /**
     * Get the display name of a file from its URI.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "Attachment"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    /**
     * Get the MIME type of a file from its URI.
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    /**
     * Determine attachment type category from MIME type.
     */
    fun getAttachmentType(mimeType: String?): String {
        return when {
            mimeType?.startsWith("image/") == true -> "image"
            mimeType == "application/pdf" -> "pdf"
            mimeType?.startsWith("text/") == true -> "text"
            mimeType?.contains("word") == true || mimeType?.contains("document") == true -> "document"
            else -> "file"
        }
    }

    /**
     * Extract text from a PDF file.
     */
    suspend fun extractTextFromPdf(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        if (!pdfBoxInitialized) {
            PDFBoxResourceLoader.init(context)
            pdfBoxInitialized = true
        }
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open PDF file")
        val document = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        document.close()
        inputStream.close()
        text.trim()
    }

    /**
     * Extract text from a text file (TXT, CSV, etc.).
     */
    suspend fun extractTextFromTextFile(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open text file")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.readText()
        reader.close()
        inputStream.close()
        content.trim()
    }

    /**
     * Convert an image URI to a Base64 data URL string suitable for the OpenRouter vision API.
     * Scales down large images to keep payload size manageable.
     */
    suspend fun imageUriToBase64(context: Context, uri: Uri, maxDimension: Int = 1024): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image file")

        // Decode bounds first
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        // Calculate scale factor
        val width = options.outWidth
        val height = options.outHeight
        var sampleSize = 1
        if (width > maxDimension || height > maxDimension) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }
        }

        // Decode with sample size
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val inputStream2 = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image file")
        val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            ?: throw Exception("Failed to decode image")
        inputStream2.close()

        // Scale down if still too large
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                if (it != bitmap) bitmap.recycle()
            }
        } else {
            bitmap
        }

        // Compress to JPEG and encode to Base64
        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        scaledBitmap.recycle()
        val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        baos.close()

        "data:image/jpeg;base64,$base64"
    }
}


