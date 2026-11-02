package com.zayaai.app

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class PPTGeneratorViewModel : ViewModel() {

    // ─── Generation State ───
    var isGenerating by mutableStateOf(false)
        private set

    var generatedOutline by mutableStateOf("")
        private set

    var pptFileUrl by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf("")
        private set

    // ─── Download State ───
    var isDownloading by mutableStateOf(false)
        private set

    var downloadComplete by mutableStateOf(false)
        private set

    var downloadedFileName by mutableStateOf("")
        private set

    var downloadProgress by mutableFloatStateOf(0f)
        private set

    /**
     *  STEP 1  —  Generate presentation content via AI, then create .pptx on backend
     */
    fun generatePresentation(topic: String, slideCount: Int = 10) {
        if (topic.isBlank()) return

        isGenerating = true
        generatedOutline = ""
        pptFileUrl = ""
        errorMessage = ""
        downloadComplete = false
        downloadedFileName = ""

        viewModelScope.launch {
            try {
                val clampedCount = slideCount.coerceIn(4, 20)

                val systemPrompt = buildString {
                    append("You are a JSON API that outputs ONLY raw JSON  —  no markdown, no backticks, no explanation.\n")
                    append("Create exactly $clampedCount slide objects for a PowerPoint about the user's topic.\n")
                    append("Rules:\n")
                    append("- Output must be a JSON array: [{...},{...},...]\n")
                    append("- Each object has exactly two keys: \"title\" (string) and \"bullets\" (array of 3-5 short strings).\n")
                    append("- Slide 1 is the TITLE slide: title = presentation title, bullets = [\"subtitle text\"].\n")
                    append("- Slide 2 is TABLE OF CONTENTS listing the upcoming slide titles as bullets.\n")
                    append("- Slides 3 to ${clampedCount - 1} are CONTENT slides with informative bullet points.\n")
                    append("- The last slide is THANK YOU: title = \"Thank You\", bullets = [\"Questions & Discussion\"].\n")
                    append("- Keep each bullet under 80 characters. Be specific and informative.\n")
                    append("- Start your response with [ and end with ]. Nothing else.")
                }

                val aiResponse = AIService.generate(
                    prompt = "Presentation topic: $topic",
                    systemPrompt = systemPrompt
                )

                Log.d("PPTGenerator", "AI raw response (first 500): ${aiResponse.take(500)}")

                // Parse the AI response into structured slides
                val slides = parseSlides(aiResponse, topic, clampedCount)

                Log.d("PPTGenerator", "Parsed ${slides.size} slides")

                // Build a human-readable outline for display
                generatedOutline = buildOutlineText(slides)

                // Send slides to backend to create the .pptx file
                pptFileUrl = createPPTOnBackend(topic, slides)

            } catch (e: Exception) {
                Log.e("PPTGenerator", "Error generating presentation", e)
                errorMessage = e.localizedMessage ?: "Failed to generate presentation."
            } finally {
                isGenerating = false
            }
        }
    }

    /**
     * Robust JSON parsing with multiple fallback strategies.
     */
    private fun parseSlides(aiResponse: String, topic: String, targetCount: Int): List<PPTSlide> {
        val gson = Gson()
        val type = object : TypeToken<List<PPTSlide>>() {}.type

        // Strategy 1: Strip code fences and try direct parse
        val cleaned = aiResponse
            .replace(Regex("```json\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("```\\s*"), "")
            .trim()

        try {
            val slides: List<PPTSlide> = gson.fromJson(cleaned, type)
            if (slides.isNotEmpty() && slides.all { it.title.isNotBlank() && it.bullets.isNotEmpty() }) {
                Log.d("PPTGenerator", "Strategy 1 succeeded: direct parse")
                return slides
            }
        } catch (_: Exception) {}

        // Strategy 2: Find the outermost [ ... ] bracket pair
        try {
            val firstBracket = cleaned.indexOf('[')
            val lastBracket = cleaned.lastIndexOf(']')
            if (firstBracket >= 0 && lastBracket > firstBracket) {
                val jsonSubstring = cleaned.substring(firstBracket, lastBracket + 1)
                val slides: List<PPTSlide> = gson.fromJson(jsonSubstring, type)
                if (slides.isNotEmpty() && slides.all { it.title.isNotBlank() }) {
                    Log.d("PPTGenerator", "Strategy 2 succeeded: bracket extraction")
                    return slides
                }
            }
        } catch (_: Exception) {}

        // Strategy 3: Use Gson JsonArray for resilient element-by-element parsing
        try {
            val firstBracket = cleaned.indexOf('[')
            val lastBracket = cleaned.lastIndexOf(']')
            if (firstBracket >= 0 && lastBracket > firstBracket) {
                val jsonSubstring = cleaned.substring(firstBracket, lastBracket + 1)
                val jsonArray = gson.fromJson(jsonSubstring, JsonArray::class.java)
                val slides = mutableListOf<PPTSlide>()
                for (i in 0 until jsonArray.size()) {
                    val obj = jsonArray.get(i).asJsonObject
                    val title = obj.get("title")?.asString ?: continue
                    val bulletsArr = obj.getAsJsonArray("bullets") ?: continue
                    val bullets = mutableListOf<String>()
                    for (j in 0 until bulletsArr.size()) {
                        val text = bulletsArr.get(j).asString?.trim()
                        if (!text.isNullOrBlank()) bullets.add(text)
                    }
                    if (title.isNotBlank() && bullets.isNotEmpty()) {
                        slides.add(PPTSlide(title.trim(), bullets))
                    }
                }
                if (slides.isNotEmpty()) {
                    Log.d("PPTGenerator", "Strategy 3 succeeded: JsonArray element-by-element")
                    return slides
                }
            }
        } catch (_: Exception) {}

        // Strategy 4: AI gave text instead of JSON  —  build slides from text
        Log.w("PPTGenerator", "All JSON strategies failed, building from text response")
        return buildSlidesFromText(aiResponse, topic, targetCount)
    }

    /**
     * If the AI returned text instead of JSON, parse headings/bullets from it.
     */
    private fun buildSlidesFromText(text: String, topic: String, targetCount: Int): List<PPTSlide> {
        val slides = mutableListOf<PPTSlide>()
        slides.add(PPTSlide(topic, listOf("AI-Generated Presentation by AI ZAYA")))

        // Try to split by numbered patterns like "Slide 1:", "1.", "##" etc.
        val sections = text.split(Regex("(?m)^(?:#{1,3}\\s+|Slide\\s*\\d+[:\\-]?\\s*|\\d+\\.\\s+)"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (section in sections) {
            val lines = section.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            val title = lines.first()
                .replace(Regex("^[\\-\\* • ]+\\s*"), "")
                .replace(Regex("^\\*\\*(.+?)\\*\\*$"), "$1")
                .trim()

            val bullets = lines.drop(1)
                .map { it.replace(Regex("^[\\-\\* • \\d.]+\\s*"), "").trim() }
                .filter { it.isNotBlank() && it.length > 2 }
                .take(5)

            if (title.isNotBlank() && title.length < 200) {
                slides.add(PPTSlide(title, if (bullets.isEmpty()) listOf("Key point about $title") else bullets))
            }

            if (slides.size >= targetCount - 1) break
        }

        // If we got very few, add generic content
        if (slides.size < 3) {
            slides.add(PPTSlide("Overview", listOf(
                "Introduction to $topic",
                "Key concepts and definitions",
                "Important aspects to understand"
            )))
            slides.add(PPTSlide("Key Points", listOf(
                "Main takeaway from $topic",
                "Supporting details and evidence",
                "Practical applications"
            )))
            slides.add(PPTSlide("Conclusion", listOf(
                "Summary of key findings",
                "Future directions",
                "Recommendations"
            )))
        }

        slides.add(PPTSlide("Thank You", listOf("Questions & Discussion")))
        return slides
    }

    private fun buildOutlineText(slides: List<PPTSlide>): String {
        return slides.mapIndexed { index, slide ->
            val header = "**Slide ${index + 1}: ${slide.title}**"
            val bullets = slide.bullets.joinToString("\n") { "   •  $it" }
            "$header\n$bullets"
        }.joinToString("\n\n")
    }

    /**
     * Calls the backend /api/ppt-create endpoint.
     * Returns the downloadable .pptx file URL.
     */
    private suspend fun createPPTOnBackend(topic: String, slides: List<PPTSlide>): String {
        val response = RetrofitClient.backendApi.createPPT(
            PPTCreateRequest(topic = topic, slides = slides)
        )

        if (response.error != null) {
            throw Exception(response.error)
        }

        return response.fileUrl
            ?: throw Exception("No file URL returned from server.")
    }

    /**
     *  STEP 2  —  Download the generated .pptx file to Downloads/AI_ZAYA/
     */
    fun downloadPPT(context: Context) {
        if (pptFileUrl.isBlank()) {
            errorMessage = "No file URL available. Generate a presentation first."
            return
        }

        isDownloading = true
        downloadProgress = 0f
        downloadComplete = false
        errorMessage = ""

        val timestamp = System.currentTimeMillis()
        val fileName = "presentation_$timestamp.pptx"
        downloadedFileName = fileName

        FileDownloadHelper.downloadFile(
            context = context,
            fileUrl = pptFileUrl,
            fileName = fileName
        ) { _, success ->
            isDownloading = false
            downloadProgress = if (success) 1f else 0f
            downloadComplete = success
            if (!success) {
                errorMessage = "Download failed. Please check your internet connection and try again."
            }
        }
    }

    /**
     *  STEP 3  —  Open the downloaded file with an external viewer
     */
    fun openDownloadedFile(context: Context) {
        if (downloadedFileName.isBlank()) {
            errorMessage = "No downloaded file to open."
            return
        }
        FileDownloadHelper.openDownloadedFile(context, downloadedFileName)
    }

    fun clear() {
        generatedOutline = ""
        pptFileUrl = ""
        errorMessage = ""
        isDownloading = false
        downloadComplete = false
        downloadedFileName = ""
        downloadProgress = 0f
    }
}






