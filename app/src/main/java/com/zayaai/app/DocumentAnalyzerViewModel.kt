package com.zayaai.app

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class DocumentAnalyzerViewModel : ViewModel() {

    // ─── State ───
    var extractedText by mutableStateOf("")
        private set

    var analysisResult by mutableStateOf("")
        private set

    var isExtracting by mutableStateOf(false)
        private set

    var isAnalyzing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var fileName by mutableStateOf("")
        private set

    var fileType by mutableStateOf("")
        private set

    private var pdfBoxInitialized = false

    // ─── PDF Extraction ───
    fun extractFromPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            isExtracting = true
            errorMessage = null
            extractedText = ""
            analysisResult = ""
            fileName = getFileName(context, uri)
            fileType = "PDF"
            try {
                val text = withContext(Dispatchers.IO) {
                    if (!pdfBoxInitialized) {
                        PDFBoxResourceLoader.init(context)
                        pdfBoxInitialized = true
                    }
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open PDF file")
                    val document = PDDocument.load(inputStream)
                    val stripper = PDFTextStripper()
                    val result = stripper.getText(document)
                    document.close()
                    inputStream.close()
                    result.trim()
                }
                if (text.isBlank()) {
                    errorMessage = "No text found in PDF. The file might be image-based  —  try uploading as image instead."
                } else {
                    extractedText = text
                }
            } catch (e: Exception) {
                errorMessage = "PDF extraction failed: ${e.message}"
            } finally {
                isExtracting = false
            }
        }
    }

    // ─── Image OCR Extraction ───
    fun extractFromImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            isExtracting = true
            errorMessage = null
            extractedText = ""
            analysisResult = ""
            fileName = getFileName(context, uri)
            fileType = "Image"
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val result = recognizer.process(image).await()
                val text = result.text.trim()
                if (text.isBlank()) {
                    errorMessage = "No text found in image. Make sure the image contains readable text."
                } else {
                    extractedText = text
                }
            } catch (e: Exception) {
                errorMessage = "OCR failed: ${e.message}"
            } finally {
                isExtracting = false
            }
        }
    }

    // ─── Text File Extraction ───
    fun extractFromTextFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            isExtracting = true
            errorMessage = null
            extractedText = ""
            analysisResult = ""
            fileName = getFileName(context, uri)
            fileType = "Text"
            try {
                val text = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open text file")
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    reader.close()
                    inputStream.close()
                    content.trim()
                }
                if (text.isBlank()) {
                    errorMessage = "The text file appears to be empty."
                } else {
                    extractedText = text
                }
            } catch (e: Exception) {
                errorMessage = "Text file read failed: ${e.message}"
            } finally {
                isExtracting = false
            }
        }
    }

    // ─── Paste Text ───
    fun setManualText(text: String) {
        extractedText = text
        fileName = "Pasted Text"
        fileType = "Manual"
        errorMessage = null
        analysisResult = ""
    }

    // ─── AI Analysis ───
    fun analyzeDocument(analysisType: String) {
        if (extractedText.isBlank()) {
            errorMessage = "No document text to analyze. Upload a document first."
            return
        }

        isAnalyzing = true
        errorMessage = null
        analysisResult = ""

        val prompt = when (analysisType) {
            "summary" -> "Summarize the following document concisely. Use bullet points for key points:\n\n$extractedText"
            "key_points" -> "Extract the key points and main ideas from this document. Use a numbered list:\n\n$extractedText"
            "explain" -> "Explain this document in simple terms that anyone can understand. Use headings and short paragraphs:\n\n$extractedText"
            "questions" -> "Generate 10 study questions (mix of short answer and MCQ) from this document. Include answers:\n\n$extractedText"
            "critique" -> "Provide a critical analysis of this document. Identify strengths, weaknesses, and suggestions for improvement:\n\n$extractedText"
            "translate" -> "Translate this document to English (if not already English) and provide a clean formatted version:\n\n$extractedText"
            "outline" -> "Create a structured outline/table of contents from this document with headings and sub-points:\n\n$extractedText"
            "custom" -> extractedText // Custom query handled differently
            else -> "Analyze this document:\n\n$extractedText"
        }

        val systemPrompt = """You are AI ZAYA, an expert document analyzer developed by ZAYA CODE HUB. 
Follow these rules:
- Give concise, well-structured responses using markdown formatting
- Use **bold** for key terms, ## headings for sections, bullet points for lists
- Wrap any code in markdown code blocks with language tags
- Be thorough but not verbose
- Focus on accuracy and clarity"""

        viewModelScope.launch {
            try {
                // Truncate text if too long (API limit)
                val truncatedText = if (prompt.length > 12000) {
                    prompt.take(12000) + "\n\n[Document truncated due to length...]"
                } else prompt

                analysisResult = AIService.generate(
                    prompt = truncatedText,
                    systemPrompt = systemPrompt
                )
            } catch (e: Exception) {
                errorMessage = "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    // ─── Custom Question ───
    fun askQuestion(question: String) {
        if (extractedText.isBlank()) {
            errorMessage = "No document text available. Upload a document first."
            return
        }
        if (question.isBlank()) {
            errorMessage = "Please enter a question."
            return
        }

        isAnalyzing = true
        errorMessage = null
        analysisResult = ""

        val prompt = """Based on the following document, answer this question: "$question"

Document:
$extractedText"""

        val systemPrompt = """You are AI ZAYA, an expert document analyzer. Answer questions based ONLY on the provided document content. 
If the answer is not in the document, say so. Use markdown formatting for clarity."""

        viewModelScope.launch {
            try {
                val truncatedPrompt = if (prompt.length > 12000) {
                    prompt.take(12000) + "\n\n[Document truncated...]"
                } else prompt

                analysisResult = AIService.generate(
                    prompt = truncatedPrompt,
                    systemPrompt = systemPrompt
                )
            } catch (e: Exception) {
                errorMessage = "Failed to answer: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    // ─── Clear ───
    fun clear() {
        extractedText = ""
        analysisResult = ""
        errorMessage = null
        fileName = ""
        fileType = ""
    }

    // ─── Helper ───
    private fun getFileName(context: Context, uri: Uri): String {
        var name = "Unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}


