package com.zayaai.app

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class ResumeTemplate(
    val id: Int,
    val name: String,
    val style: String,       // "Modern", "Classic", "Creative", "Minimal", "ATS-Optimized"
    val description: String,
    val sections: List<String>,
    val color: Long           // hex as long for accent
)

class ResumeViewModel : ViewModel() {

    // ── Templates ──
    val templates = listOf(
        ResumeTemplate(1, "Modern Professional", "Modern",
            "Clean layout with sidebar  —  great for tech & business roles",
            listOf("Profile Summary", "Experience", "Education", "Skills", "Projects", "Certifications"),
            0xFF6C63FF),
        ResumeTemplate(2, "Classic ATS-Friendly", "ATS-Optimized",
            "Simple, single-column format that passes all ATS scanners",
            listOf("Objective", "Experience", "Education", "Skills", "Awards"),
            0xFF1565C0),
        ResumeTemplate(3, "Creative Designer", "Creative",
            "Two-column with visual flair  —  ideal for design & media roles",
            listOf("About Me", "Portfolio Highlights", "Experience", "Skills", "Education", "Interests"),
            0xFFE91E63),
        ResumeTemplate(4, "Minimal Clean", "Minimal",
            "Minimalist design with lots of whitespace  —  elegant & professional",
            listOf("Summary", "Experience", "Education", "Skills"),
            0xFF37474F),
        ResumeTemplate(5, "Student / Fresher", "Modern",
            "Optimized for students  —  highlights education, projects & skills",
            listOf("Career Objective", "Education", "Projects", "Skills", "Internships", "Extra-curricular"),
            0xFF4CAF50),
        ResumeTemplate(6, "Executive", "Classic",
            "Senior-level resume with leadership focus & strategic summary",
            listOf("Executive Summary", "Core Competencies", "Professional Experience", "Education", "Board Memberships"),
            0xFF9D50BB),
        ResumeTemplate(7, "Tech / Developer", "Modern",
            "Built for developers  —  highlights tech stack, GitHub, and projects",
            listOf("Summary", "Technical Skills", "Projects", "Experience", "Education", "Open Source"),
            0xFF00BCD4),
        ResumeTemplate(8, "Internship Ready", "ATS-Optimized",
            "Perfect for first-time job seekers and internship applications",
            listOf("Objective", "Education", "Skills", "Projects", "Volunteer Work", "References"),
            0xFFFF9800)
    )

    // ── Template Fill ──
    private val _templateContent = MutableStateFlow("")
    val templateContent: StateFlow<String> = _templateContent

    private val _isGeneratingTemplate = MutableStateFlow(false)
    val isGeneratingTemplate: StateFlow<Boolean> = _isGeneratingTemplate

    fun generateFromTemplate(template: ResumeTemplate, userDetails: String) {
        if (userDetails.isBlank()) return
        viewModelScope.launch {
            _isGeneratingTemplate.value = true
            try {
                val result = AIService.generate(
                    prompt = "Generate a complete, ATS-friendly resume using the following details:\n\n$userDetails",
                    systemPrompt = """You are a professional resume writer. Generate a polished, ATS-friendly resume using the "${template.name}" template style (${template.style}).

The resume MUST include these sections: ${template.sections.joinToString(", ")}

Formatting rules:
- Use Markdown with clear headings (##), bold (**text**), and bullet points
- Keep it professional, concise, and impactful
- Use action verbs and quantifiable achievements
- Optimize keywords for ATS (Applicant Tracking Systems)
- Include relevant industry keywords naturally
- Keep sentences short and scannable
- Format dates consistently (e.g., Jan 2024  –  Present)

Output ONLY the resume content in clean Markdown. Do not add comments or explanations."""
                )
                _templateContent.value = result
            } catch (e: Exception) {
                _templateContent.value = "Error: ${e.localizedMessage ?: "Failed to generate resume"}"
            } finally {
                _isGeneratingTemplate.value = false
            }
        }
    }

    // ── AI Resume Builder (from scratch) ──
    private val _generatedResume = MutableStateFlow("")
    val generatedResume: StateFlow<String> = _generatedResume

    private val _isGeneratingResume = MutableStateFlow(false)
    val isGeneratingResume: StateFlow<Boolean> = _isGeneratingResume

    fun generateResume(details: String) {
        if (details.isBlank()) return
        viewModelScope.launch {
            _isGeneratingResume.value = true
            try {
                val result = AIService.generate(
                    prompt = "Create a complete professional resume from these details:\n\n$details",
                    systemPrompt = """You are an expert resume writer specializing in ATS-optimized resumes. Create a complete, professional resume from the user's details.

Structure the resume with these sections (include only relevant ones):
## [FULL NAME]
**Contact:** email | phone | LinkedIn | location

### Professional Summary
2-3 powerful sentences highlighting key strengths and career goals.

### Skills
Organized by category (Technical, Soft Skills, Tools, etc.)  —  use comma-separated lists.

### Professional Experience
For each role:
**Job Title**  —  Company Name | Location | Dates
- Achievement-focused bullet points with metrics/numbers
- Start each bullet with a strong action verb

### Education
**Degree**  —  Institution | Year
Include GPA if above 3.5, relevant coursework, honors

### Projects (if applicable)
**Project Name**  —  Brief description with tech stack and impact

### Certifications (if applicable)

### Extra-curricular / Volunteer (if applicable)

ATS Optimization Rules:
- Use standard section headings (no creative names)
- Include relevant keywords from common job descriptions
- No tables, columns, or graphics (ATS can't read them)
- Use simple bullet points ( • , -, *)
- Spell out acronyms at least once
- Use both long-form and abbreviations for skills (e.g., "JavaScript (JS)")

Output ONLY the resume in clean Markdown. Make it impressive, professional, and ready to submit."""
                )
                _generatedResume.value = result
            } catch (e: Exception) {
                _generatedResume.value = "Error: ${e.localizedMessage ?: "Failed to generate resume"}"
            } finally {
                _isGeneratingResume.value = false
            }
        }
    }

    // ── Resume Analyzer ──
    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    fun analyzeResume(resumeText: String) {
        if (resumeText.isBlank()) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = AIService.generate(
                    prompt = "Analyze this resume and suggest matching jobs:\n\n$resumeText",
                    systemPrompt = """You are a career counselor and resume analyst. Analyze the given resume text thoroughly and provide a comprehensive report in Markdown format:

## ðŸ“Š Resume Score
Give an overall score out of 100 with a brief explanation.

## âœ… Strengths
List 3-5 strengths of this resume.

## âš ï¸ Areas for Improvement
List 3-5 specific suggestions to improve the resume.

## ðŸŽ¯ Best Matching Job Roles
Based on the skills, experience, and education in the resume, suggest **8-10 specific job titles** that would be the best fit. For each:
- **Job Title**  —  Why it's a good match (1 sentence)
- Expected salary range (approximate, in USD)

## ðŸ¢ Companies to Target
Suggest 5-8 companies (mix of startups, mid-size, and large) that would be good targets based on the candidate's profile.

## ðŸ”‘ Missing Keywords
List important industry keywords that are missing from the resume but would help pass ATS filters.

## ðŸ“ˆ Career Growth Path
Suggest a 3-5 year career progression path based on current skills.

## ðŸ’¡ Quick Wins
3 things the candidate can do RIGHT NOW to improve their chances (courses, certifications, portfolio additions).

Be specific, actionable, and encouraging. Use emojis for section headers."""
                )
                _analysisResult.value = result
            } catch (e: Exception) {
                _analysisResult.value = "Error: ${e.localizedMessage ?: "Failed to analyze resume"}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearTemplate() { _templateContent.value = "" }
    fun clearResume() { _generatedResume.value = "" }
    fun clearAnalysis() { _analysisResult.value = "" }

    // ── File Upload State ──
    private val _uploadedFileName = MutableStateFlow<String?>(null)
    val uploadedFileName: StateFlow<String?> = _uploadedFileName

    private val _uploadedFileType = MutableStateFlow<String?>(null)
    val uploadedFileType: StateFlow<String?> = _uploadedFileType

    private val _extractedResumeText = MutableStateFlow("")
    val extractedResumeText: StateFlow<String> = _extractedResumeText

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting

    /**
     * Upload and extract text from a resume file (PDF, DOC, TXT, image).
     * The extracted text is stored for the user to review before analyzing.
     */
    fun uploadResumeFile(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isExtracting.value = true
            try {
                val mimeType = FileProcessingUtils.getMimeType(context, uri)
                val attachmentType = FileProcessingUtils.getAttachmentType(mimeType)
                val fileName = FileProcessingUtils.getFileNameFromUri(context, uri)

                _uploadedFileName.value = fileName
                _uploadedFileType.value = attachmentType

                val extractedText = when (attachmentType) {
                    "pdf" -> FileProcessingUtils.extractTextFromPdf(context, uri)
                    "image" -> extractTextFromImage(uri, context)
                    else -> FileProcessingUtils.extractTextFromTextFile(context, uri)
                }

                if (extractedText.isBlank()) {
                    _extractedResumeText.value = ""
                    _analysisResult.value = "âš ï¸ Could not extract text from **$fileName**. The file may be empty or image-based. Try uploading a different format or paste your resume text manually."
                } else {
                    _extractedResumeText.value = extractedText
                }
            } catch (e: Exception) {
                _extractedResumeText.value = ""
                _analysisResult.value = "âŒ Error reading file: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isExtracting.value = false
            }
        }
    }

    /**
     * Extract text from an image-based resume using on-device ML Kit OCR.
     */
    private suspend fun extractTextFromImage(uri: Uri, context: Context): String {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            result.text.trim()
        } catch (e: Exception) {
            ""
        }
    }

    fun clearUploadedFile() {
        _uploadedFileName.value = null
        _uploadedFileType.value = null
        _extractedResumeText.value = ""
    }
}


