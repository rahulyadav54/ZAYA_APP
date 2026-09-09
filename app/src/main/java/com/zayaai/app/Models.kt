package com.zayaai.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Voice : Screen("voice", "Voice Assistant", Icons.Rounded.Mic)
    object History : Screen("history", "Chat History", Icons.Rounded.History)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    
    // Main Tools
    object AIChat : Screen("ai_chat", "AI Chat", Icons.Rounded.Chat)
    object ImageGenerator : Screen("image_gen", "Image Generator", Icons.Rounded.Image)
    object CodeGenerator : Screen("code_gen", "Code Generator", Icons.Rounded.Code)
    object TextWriter : Screen("text_writer", "Text Writer", Icons.Rounded.Edit)
    object QuizGenerator : Screen("quiz_gen", "Quiz Generator", Icons.Rounded.Quiz)
    object DocumentAnalyzer : Screen("doc_analyzer", "Document Analyzer", Icons.Rounded.Description)
    object PPTGenerator : Screen("ppt_gen", "PPT Generator", Icons.Rounded.Slideshow)
    object PromptEngineer : Screen("prompt_engineer", "Prompt Engineer", Icons.Rounded.AutoFixHigh)
    
    // Student Tools Section
    object StudentTools : Screen("student_tools", "Student Tools", Icons.Rounded.School)
    object Notes : Screen("notes", "Notes", Icons.Rounded.Notes)
    object Flashcards : Screen("flashcards", "Flashcards", Icons.Rounded.Style)
    object OfflineQuiz : Screen("offline_quiz", "Offline Quiz", Icons.Rounded.Quiz)
    object FormulaLibrary : Screen("formulas", "Formula Library", Icons.Rounded.Functions)
    object MindMap : Screen("mind_map", "Mind Map", Icons.Rounded.AccountTree)
    object StudyPlanner : Screen("study_planner", "Study Planner", Icons.Rounded.CalendarMonth)
    object Pomodoro : Screen("pomodoro", "Pomodoro Timer", Icons.Rounded.Timer)
    object ToDo : Screen("todo", "To-Do Manager", Icons.AutoMirrored.Rounded.Assignment)
    object Calculator : Screen("calculator", "Scientific Calculator", Icons.Rounded.Calculate)
    object UnitConverter : Screen("unit_converter", "Unit Converter", Icons.Rounded.SwapHoriz)
    object WordCounter : Screen("word_counter", "Word Counter", Icons.Rounded.Abc)
    object TextFormatter : Screen("text_formatter", "Text Formatter", Icons.Rounded.TextFormat)
    object Dictionary : Screen("dictionary", "Dictionary", Icons.Rounded.Book)
    object BookmarkManager : Screen("bookmarks", "Bookmark Manager", Icons.Rounded.Bookmarks)
    object StudyArticles : Screen("articles", "Study Articles", Icons.AutoMirrored.Rounded.Article)
    object OfflineAssistant : Screen("offline_assistant", "Offline AI Assistant", Icons.Rounded.SmartToy)

    // Productivity
    object PromptLibrary : Screen("prompt_lib", "Prompt Library", Icons.Rounded.LibraryBooks)
    object StartupIdea : Screen("startup_idea", "Startup Idea", Icons.Rounded.Lightbulb)
    object ResumeBuilder : Screen("resume_builder", "Resume Builder", Icons.Rounded.Description)
    object StudyTools : Screen("study_tools", "AI Study Tools", Icons.Rounded.School)
    
    // App Settings
    object AppLock : Screen("app_lock", "App Lock", Icons.Rounded.Lock)
    object About : Screen("about", "About App", Icons.Rounded.Info)

    // Redirects
    object StudyToolsRedirect : Screen("study_tools_redirect", "Study Tools Redirect", Icons.Rounded.School)

    // Admin Management
    object AdminDashboard : Screen("admin_dashboard", "Admin Dashboard", Icons.Rounded.AdminPanelSettings)
    object QuizManagement : Screen("quiz_management", "Quiz Management", Icons.Rounded.Quiz)
    object FormulaManagement : Screen("formula_management", "Formula Management", Icons.Rounded.Functions)
    object AnnouncementManagement : Screen("announcement_management", "Announcement Management", Icons.Rounded.Campaign)
    object ArticleManagement : Screen("article_management", "Article Management", Icons.AutoMirrored.Rounded.Article)
    object AdminStudyMaterials : Screen("admin_study_materials", "Study Materials Admin", Icons.Rounded.Folder)

    // AI Learning Features
    object UploadStudyMaterial : Screen("upload_study_material", "Upload Material", Icons.Rounded.UploadFile)
    object GeneratedQuestions : Screen("generated_questions", "Questions", Icons.Rounded.HelpOutline)
    object AIQuiz : Screen("ai_quiz", "AI Quiz", Icons.Rounded.Quiz)
    object AIFlashcards : Screen("ai_flashcards", "AI Flashcards", Icons.Rounded.Style)
    object StudySummary : Screen("study_summary", "Study Summary", Icons.Rounded.Summarize)
    object AITutor : Screen("ai_tutor", "AI Tutor", Icons.Rounded.School)
    object MaterialDetail : Screen("material_detail", "Material Detail", Icons.Rounded.Description)
    object WebBrowser : Screen("web_browser", "Web Browser", Icons.Rounded.Public)

    // Brain Games
    object BrainGames : Screen("brain_games", "Brain Games", Icons.Rounded.SportsEsports)
    object MathChallenge : Screen("math_challenge", "Math Challenge", Icons.Rounded.Calculate)
    object MemoryMatrix : Screen("memory_matrix", "Memory Matrix", Icons.Rounded.GridView)
    object WordScramble : Screen("word_scramble", "Word Scramble", Icons.Rounded.TextRotationNone)
    object NumberSlider : Screen("number_slider", "15 Puzzle", Icons.Rounded.ViewModule)
    object EmojiMatch : Screen("emoji_match", "Emoji Match", Icons.Rounded.EmojiEmotions)
    object ColorRush : Screen("color_rush", "Color Rush", Icons.Rounded.Palette)
}

data class Tool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

enum class AuthScreen { Login, Register, ForgotPassword }
enum class ActiveTab { Home, Voice, History, Settings }
enum class ActiveTool { None, Chat, TextWriter, CodeGen, QuizGen }

// Firestore Models
data class FirestoreQuiz(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val questions: List<QuizQuestion> = emptyList()
)

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0
)

data class FirestoreFormula(
    val id: String = "",
    val title: String = "",
    val formula: String = "",
    val category: String = ""
)

data class FirestoreAnnouncement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class FirestoreArticle(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = ""
)

// â”€â”€â”€ AI Learning Feature Firestore Models â”€â”€â”€

data class FirestoreStudyMaterial(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val title: String = "",
    val fileType: String = "",
    val extractedText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class FirestoreGeneratedQuestion(
    val id: String = "",
    val materialId: String = "",
    val question: String = "",
    val answer: String = ""
)

data class FirestoreAIQuizQuestion(
    val id: String = "",
    val materialId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val questionType: String = "mcq",
    val difficulty: String = "medium"
)

data class FirestoreAIFlashcard(
    val id: String = "",
    val materialId: String = "",
    val frontText: String = "",
    val backText: String = ""
)

data class FirestoreStudySummary(
    val id: String = "",
    val materialId: String = "",
    val summary: String = ""
)

data class FirestoreUserPerformance(
    val id: String = "",
    val userId: String = "",
    val materialId: String = "",
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val difficulty: String = "medium",
    val timestamp: Long = System.currentTimeMillis()
)


