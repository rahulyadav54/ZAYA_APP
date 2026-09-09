package com.zayaai.app

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class StudyMaterialViewModel(
    private val repository: AiZayaRepository,
    private val db: AiZayaDatabase
) : ViewModel() {


    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // â”€â”€â”€ State â”€â”€â”€
    var isProcessing by mutableStateOf(false)
        private set
    var processingStep by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var extractedText by mutableStateOf("")
        private set

    // Current material being worked on
    var currentMaterialId by mutableStateOf<Long?>(null)
        private set
    var currentMaterialTitle by mutableStateOf("")
        private set

    // Generated content
    val generatedQuestions = mutableStateListOf<GeneratedQuestionEntity>()
    val quizQuestions = mutableStateListOf<AIQuizQuestionEntity>()
    val flashcards = mutableStateListOf<AIFlashcardEntity>()
    var studySummary by mutableStateOf<String?>(null)
        private set

    // Materials list
    private val _materials = MutableStateFlow<List<StudyMaterialEntity>>(emptyList())
    val materials: StateFlow<List<StudyMaterialEntity>> = _materials

    // Quiz state
    var currentQuizIndex by mutableStateOf(0)
        private set
    var quizScore by mutableStateOf(0)
        private set
    var quizCompleted by mutableStateOf(false)
        private set
    var selectedAnswer by mutableStateOf<String?>(null)
        private set
    var showAnswer by mutableStateOf(false)
        private set
    var currentDifficulty by mutableStateOf("medium")
        private set

    // AI Tutor chat
    val tutorMessages = mutableStateListOf<Pair<String, Boolean>>() // text, isUser
    var tutorInput by mutableStateOf("")
    var isTutorProcessing by mutableStateOf(false)
        private set

    private var pdfBoxInitialized = false

    fun clearError() { errorMessage = null }

    // â”€â”€â”€ Load Materials â”€â”€â”€
    fun loadMaterials() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            db.studyMaterialDao().getMaterialsByUser(userId).collect { list ->
                _materials.value = list
            }
        }
    }

    // â”€â”€â”€ Text Extraction â”€â”€â”€

    fun extractTextFromPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            isProcessing = true
            processingStep = "Extracting text from PDF..."
            errorMessage = null
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
                    errorMessage = "No text found in PDF. The file might be image-based."
                } else {
                    extractedText = text
                }
            } catch (e: Exception) {
                errorMessage = "PDF extraction failed: ${e.message}"
            } finally {
                isProcessing = false
                processingStep = ""
            }
        }
    }

    fun extractTextFromImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            isProcessing = true
            processingStep = "Running OCR on image..."
            errorMessage = null
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val result = recognizer.process(image).await()
                val text = result.text.trim()
                if (text.isBlank()) {
                    errorMessage = "No text found in image."
                } else {
                    extractedText = text
                }
            } catch (e: Exception) {
                errorMessage = "OCR failed: ${e.message}"
            } finally {
                isProcessing = false
                processingStep = ""
            }
        }
    }

    fun extractTextFromTextFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            isProcessing = true
            processingStep = "Reading text file..."
            errorMessage = null
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
                    errorMessage = "Text file is empty."
                } else {
                    extractedText = text
                }
            } catch (e: Exception) {
                errorMessage = "Failed to read text file: ${e.message}"
            } finally {
                isProcessing = false
                processingStep = ""
            }
        }
    }

    fun setManualText(text: String) {
        extractedText = text
    }

    // â”€â”€â”€ Full AI Processing Pipeline â”€â”€â”€

    fun processStudyMaterial(title: String, fileType: String) {
        if (extractedText.isBlank()) {
            errorMessage = "No text to process. Please upload or paste content first."
            return
        }
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            try {
                // 1. Save material locally
                processingStep = "Saving material..."
                val userId = auth.currentUser?.uid ?: "anonymous"
                val materialEntity = StudyMaterialEntity(
                    userId = userId,
                    title = title.ifBlank { "Untitled Material" },
                    fileType = fileType,
                    extractedText = extractedText
                )
                val materialId = db.studyMaterialDao().insertMaterial(materialEntity)
                currentMaterialId = materialId
                currentMaterialTitle = materialEntity.title

                // 2. Save to Firestore
                processingStep = "Syncing to cloud..."
                saveToFirestore(materialId, materialEntity)

                // Truncate text for AI processing if too long
                val textForAI = if (extractedText.length > 15000) {
                    extractedText.take(15000) + "\n\n[Content truncated for processing]"
                } else {
                    extractedText
                }

                // 3. Generate Questions
                processingStep = "AI is generating important questions..."
                generateQuestions(materialId, textForAI)

                // 4. Generate Quiz
                processingStep = "AI is creating quiz..."
                generateQuiz(materialId, textForAI)

                // 5. Generate Flashcards
                processingStep = "AI is building flashcards..."
                generateFlashcards(materialId, textForAI)

                // 6. Generate Summary
                processingStep = "AI is writing study summary..."
                generateSummary(materialId, textForAI)

                processingStep = "All done!"
            } catch (e: Exception) {
                errorMessage = "Processing failed: ${e.message}"
            } finally {
                isProcessing = false
                processingStep = ""
            }
        }
    }

    private suspend fun saveToFirestore(localId: Long, entity: StudyMaterialEntity) {
        try {
            val ref = firestore.collection("study_materials").document()
            val fsDoc = FirestoreStudyMaterial(
                id = ref.id,
                userId = entity.userId,
                userEmail = auth.currentUser?.email ?: "",
                title = entity.title,
                fileType = entity.fileType,
                extractedText = if (entity.extractedText.length > 50000) entity.extractedText.take(50000) else entity.extractedText,
                timestamp = entity.timestamp
            )
            ref.set(fsDoc).await()
            // Update local record with Firestore ID
            db.studyMaterialDao().insertMaterial(entity.copy(id = localId, firestoreId = ref.id))
        } catch (_: Exception) {
            // Non-critical â€” local copy is the source of truth
        }
    }

    // â”€â”€â”€ AI Generation Functions â”€â”€â”€

    private suspend fun generateQuestions(materialId: Long, text: String) {
        val prompt = """Analyze the following educational content and generate the most important questions with answers.
Focus on key concepts and exam-relevant topics.
Generate 5-10 questions with answers.

IMPORTANT: Return ONLY a valid JSON array. No markdown, no code blocks, no extra text.
Format:
[{"question":"...","answer":"..."},{"question":"...","answer":"..."}]

Content:
$text"""

        try {
            val raw = AIService.generate(prompt).trim()
            val jsonStr = extractJsonArray(raw)
            val arr = JSONArray(jsonStr)
            val questions = mutableListOf<GeneratedQuestionEntity>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                questions.add(
                    GeneratedQuestionEntity(
                        materialId = materialId,
                        question = obj.optString("question", ""),
                        answer = obj.optString("answer", "")
                    )
                )
            }
            db.generatedQuestionDao().insertAll(questions)
            generatedQuestions.clear()
            generatedQuestions.addAll(questions)

            // Also save to Firestore
            questions.forEach { q ->
                try {
                    val ref = firestore.collection("generated_questions").document()
                    ref.set(
                        FirestoreGeneratedQuestion(
                            id = ref.id,
                            materialId = materialId.toString(),
                            question = q.question,
                            answer = q.answer
                        )
                    ).await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            errorMessage = "Question generation failed: ${e.message}"
        }
    }

    private suspend fun generateQuiz(materialId: Long, text: String) {
        val prompt = """Generate a quiz from the following study content.

Include: 5 MCQs, 3 True/False questions, 2 short-answer questions.

IMPORTANT: Return ONLY a valid JSON array. No markdown, no code blocks.
Format for MCQ: {"question":"...","options":["A","B","C","D"],"correctAnswer":"A","type":"mcq","difficulty":"medium"}
Format for T/F: {"question":"...","options":["True","False"],"correctAnswer":"True","type":"true_false","difficulty":"easy"}
Format for Short: {"question":"...","options":[],"correctAnswer":"answer text","type":"short_answer","difficulty":"hard"}

Content:
$text"""

        try {
            val raw = AIService.generate(prompt).trim()
            val jsonStr = extractJsonArray(raw)
            val arr = JSONArray(jsonStr)
            val questions = mutableListOf<AIQuizQuestionEntity>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val opts = obj.optJSONArray("options") ?: JSONArray()
                questions.add(
                    AIQuizQuestionEntity(
                        materialId = materialId,
                        question = obj.optString("question", ""),
                        optionA = if (opts.length() > 0) opts.getString(0) else "",
                        optionB = if (opts.length() > 1) opts.getString(1) else "",
                        optionC = if (opts.length() > 2) opts.getString(2) else "",
                        optionD = if (opts.length() > 3) opts.getString(3) else "",
                        correctAnswer = obj.optString("correctAnswer", ""),
                        questionType = obj.optString("type", "mcq"),
                        difficulty = obj.optString("difficulty", "medium")
                    )
                )
            }
            db.aiQuizQuestionDao().insertAll(questions)
            quizQuestions.clear()
            quizQuestions.addAll(questions)

            // Save to Firestore
            questions.forEach { q ->
                try {
                    val ref = firestore.collection("ai_quiz_questions").document()
                    ref.set(
                        FirestoreAIQuizQuestion(
                            id = ref.id,
                            materialId = materialId.toString(),
                            question = q.question,
                            options = listOfNotNull(
                                q.optionA.ifBlank { null },
                                q.optionB.ifBlank { null },
                                q.optionC.ifBlank { null },
                                q.optionD.ifBlank { null }
                            ),
                            correctAnswer = q.correctAnswer,
                            questionType = q.questionType,
                            difficulty = q.difficulty
                        )
                    ).await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            errorMessage = "Quiz generation failed: ${e.message}"
        }
    }

    private suspend fun generateFlashcards(materialId: Long, text: String) {
        val prompt = """Convert the following study material into flashcards.
Each flashcard must have a front (question or concept) and back (explanation or answer).
Generate 8-12 flashcards.

IMPORTANT: Return ONLY a valid JSON array. No markdown, no code blocks.
Format: [{"front":"...","back":"..."}]

Content:
$text"""

        try {
            val raw = AIService.generate(prompt).trim()
            val jsonStr = extractJsonArray(raw)
            val arr = JSONArray(jsonStr)
            val cards = mutableListOf<AIFlashcardEntity>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                cards.add(
                    AIFlashcardEntity(
                        materialId = materialId,
                        frontText = obj.optString("front", ""),
                        backText = obj.optString("back", "")
                    )
                )
            }
            db.aiFlashcardDao().insertAll(cards)
            flashcards.clear()
            flashcards.addAll(cards)

            // Firestore
            cards.forEach { c ->
                try {
                    val ref = firestore.collection("ai_flashcards").document()
                    ref.set(
                        FirestoreAIFlashcard(
                            id = ref.id,
                            materialId = materialId.toString(),
                            frontText = c.frontText,
                            backText = c.backText
                        )
                    ).await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            errorMessage = "Flashcard generation failed: ${e.message}"
        }
    }

    private suspend fun generateSummary(materialId: Long, text: String) {
        val prompt = """Summarize the following educational content into clear bullet points for students.
Include:
â€¢ Key concepts
â€¢ Important definitions
â€¢ Main takeaways

Keep it concise and student-friendly.

Content:
$text"""

        try {
            val summary = AIService.generate(prompt).trim()
            db.studySummaryDao().insertSummary(
                StudySummaryEntity(materialId = materialId, summary = summary)
            )
            studySummary = summary

            // Firestore
            try {
                val ref = firestore.collection("study_summaries").document()
                ref.set(
                    FirestoreStudySummary(id = ref.id, materialId = materialId.toString(), summary = summary)
                ).await()
            } catch (_: Exception) {}
        } catch (e: Exception) {
            errorMessage = "Summary generation failed: ${e.message}"
        }
    }

    // â”€â”€â”€ Load Generated Content for a Material â”€â”€â”€

    fun loadContentForMaterial(materialId: Long) {
        currentMaterialId = materialId
        viewModelScope.launch {
            val material = db.studyMaterialDao().getMaterialById(materialId)
            currentMaterialTitle = material?.title ?: "Material"
            extractedText = material?.extractedText ?: ""

            // Load questions
            db.generatedQuestionDao().getQuestionsForMaterial(materialId).collect { list ->
                generatedQuestions.clear()
                generatedQuestions.addAll(list)
            }
        }
        viewModelScope.launch {
            db.aiQuizQuestionDao().getQuizForMaterial(materialId).collect { list ->
                quizQuestions.clear()
                quizQuestions.addAll(list)
            }
        }
        viewModelScope.launch {
            db.aiFlashcardDao().getFlashcardsForMaterial(materialId).collect { list ->
                flashcards.clear()
                flashcards.addAll(list)
            }
        }
        viewModelScope.launch {
            val summary = db.studySummaryDao().getSummaryForMaterial(materialId)
            studySummary = summary?.summary
        }
        // Load adaptive difficulty
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val perf = db.userPerformanceDao().getLatestPerformance(userId, materialId)
            currentDifficulty = perf?.difficulty ?: "medium"
        }
    }

    // â”€â”€â”€ Quiz Logic with Adaptive Difficulty â”€â”€â”€

    fun startQuiz() {
        currentQuizIndex = 0
        quizScore = 0
        quizCompleted = false
        selectedAnswer = null
        showAnswer = false
    }

    fun selectAnswer(answer: String) {
        selectedAnswer = answer
    }

    fun submitAnswer() {
        if (selectedAnswer == null || quizQuestions.isEmpty()) return
        showAnswer = true
        val current = quizQuestions[currentQuizIndex]
        if (selectedAnswer.equals(current.correctAnswer, ignoreCase = true)) {
            quizScore++
        }
    }

    fun nextQuestion() {
        showAnswer = false
        selectedAnswer = null
        if (currentQuizIndex < quizQuestions.size - 1) {
            currentQuizIndex++
        } else {
            quizCompleted = true
            savePerformanceAndAdapt()
        }
    }

    private fun savePerformanceAndAdapt() {
        val materialId = currentMaterialId ?: return
        val userId = auth.currentUser?.uid ?: return
        val total = quizQuestions.size
        val correct = quizScore
        val scorePercent = if (total > 0) (correct * 100) / total else 0

        // Adaptive difficulty logic
        val newDifficulty = when {
            scorePercent > 80 -> when (currentDifficulty) {
                "easy" -> "medium"
                "medium" -> "hard"
                else -> "hard"
            }
            scorePercent < 50 -> when (currentDifficulty) {
                "hard" -> "medium"
                "medium" -> "easy"
                else -> "easy"
            }
            else -> currentDifficulty
        }
        currentDifficulty = newDifficulty

        viewModelScope.launch {
            db.userPerformanceDao().insertPerformance(
                UserPerformanceEntity(
                    userId = userId,
                    materialId = materialId,
                    totalQuestions = total,
                    correctAnswers = correct,
                    difficulty = newDifficulty
                )
            )
            // Firestore
            try {
                val ref = firestore.collection("user_performance").document()
                ref.set(
                    FirestoreUserPerformance(
                        id = ref.id,
                        userId = userId,
                        materialId = materialId.toString(),
                        totalQuestions = total,
                        correctAnswers = correct,
                        difficulty = newDifficulty
                    )
                ).await()
            } catch (_: Exception) {}
        }
    }

    // â”€â”€â”€ AI Tutor Chat â”€â”€â”€

    fun sendTutorMessage() {
        val msg = tutorInput.trim()
        if (msg.isBlank() || extractedText.isBlank()) return
        tutorInput = ""
        tutorMessages.add(Pair(msg, true))

        viewModelScope.launch {
            isTutorProcessing = true
            try {
                val contextSnippet = if (extractedText.length > 8000) extractedText.take(8000) else extractedText
                val prompt = """You are an AI tutor helping a student understand their study material.
Answer the student's question based ONLY on the following study content.
Be educational, clear, and concise. Use simple language.

Study Material:
$contextSnippet

Student's Question: $msg"""

                val answer = AIService.generate(prompt).trim()
                tutorMessages.add(Pair(answer, false))
            } catch (e: Exception) {
                tutorMessages.add(Pair("Error: ${e.message}", false))
            } finally {
                isTutorProcessing = false
            }
        }
    }

    // â”€â”€â”€ Delete Material â”€â”€â”€

    fun deleteMaterial(material: StudyMaterialEntity) {
        viewModelScope.launch {
            db.studyMaterialDao().deleteMaterial(material)
            db.generatedQuestionDao().deleteForMaterial(material.id)
            db.aiQuizQuestionDao().deleteForMaterial(material.id)
            db.aiFlashcardDao().deleteForMaterial(material.id)
            db.studySummaryDao().deleteForMaterial(material.id)
            // Firestore cleanup
            if (material.firestoreId.isNotBlank()) {
                try {
                    firestore.collection("study_materials").document(material.firestoreId).delete().await()
                } catch (_: Exception) {}
            }
        }
    }

    // â”€â”€â”€ Reset for new upload â”€â”€â”€

    fun resetForNewUpload() {
        extractedText = ""
        currentMaterialId = null
        currentMaterialTitle = ""
        generatedQuestions.clear()
        quizQuestions.clear()
        flashcards.clear()
        studySummary = null
        errorMessage = null
        tutorMessages.clear()
        quizCompleted = false
        currentQuizIndex = 0
        quizScore = 0
    }

    // â”€â”€â”€ Helper: Extract JSON array from AI response â”€â”€â”€

    private fun extractJsonArray(raw: String): String {
        // Remove markdown code fences if present
        var cleaned = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()
        // Find first [ and last ]
        val start = cleaned.indexOf('[')
        val end = cleaned.lastIndexOf(']')
        if (start != -1 && end != -1 && end > start) {
            cleaned = cleaned.substring(start, end + 1)
        }
        return cleaned
    }
}


