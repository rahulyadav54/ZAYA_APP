package com.zayaai.app

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
    textWriterViewModel: TextWriterViewModel,
    voiceViewModel: VoiceViewModel,
    securityViewModel: SecurityViewModel,
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    userName: String,
    userEmail: String, // Re-added userEmail parameter
    userRole: String,
    viewModelFactory: ViewModelFactory,
    onProfileUpdate: () -> Unit,
    onToolClick: (Tool) -> Unit
) {
    val context = LocalContext.current
    
    // ViewModels for Main Tools
    val codeGeneratorViewModel: CodeGeneratorViewModel = viewModel()
    val quizGeneratorViewModel: QuizGeneratorViewModel = viewModel()
    val imageGeneratorViewModel: ImageGeneratorViewModel = viewModel()
    val startupIdeaViewModel: StartupIdeaViewModel = viewModel()
    val pptGeneratorViewModel: PPTGeneratorViewModel = viewModel()
    val promptEngineerViewModel: PromptEngineerViewModel = viewModel()
    val documentAnalyzerViewModel: DocumentAnalyzerViewModel = viewModel()

    // Student Tools ViewModels (Using Factory to provide Repository)
    val notesViewModel: NotesViewModel = viewModel(factory = viewModelFactory)
    val flashcardsViewModel: FlashcardsViewModel = viewModel(factory = viewModelFactory)
    val offlineQuizViewModel: QuizViewModel = viewModel(factory = viewModelFactory)
    val toDoViewModel: ToDoViewModel = viewModel(factory = viewModelFactory)
    val studyPlannerViewModel: StudyPlannerViewModel = viewModel(factory = viewModelFactory)
    val mindMapViewModel: MindMapViewModel = viewModel(factory = viewModelFactory)
    val formulaViewModel: FormulaViewModel = viewModel(factory = viewModelFactory)
    val dictionaryViewModel: DictionaryViewModel = viewModel(factory = viewModelFactory)
    val bookmarkViewModel: BookmarkViewModel = viewModel(factory = viewModelFactory)
    val articlesViewModel: StudyArticlesViewModel = viewModel(factory = viewModelFactory)

    // Others
    val pomodoroViewModel: PomodoroViewModel = viewModel()
    val calculatorViewModel: CalculatorViewModel = viewModel()
    val unitConverterViewModel: UnitConverterViewModel = viewModel()

    // AI Learning Feature ViewModel
    val db = remember(context) { AiZayaDatabase.getDatabase(context) }
    val repo = remember(db) { AiZayaRepository(db) }
    val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
    val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)

    NavHost(
        navController = navController,
        startDestination = if (userRole == "admin") Screen.AdminDashboard.route else Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(userName = userName, innerPadding = paddingValues, onToolClick = onToolClick)
        }
        composable(Screen.Voice.route) {
            VoiceAssistantScreen(viewModel = voiceViewModel, paddingValues = paddingValues)
        }
        composable(Screen.History.route) {
            ConversationHistoryScreen(
                chatViewModel = chatViewModel,
                paddingValues = paddingValues,
                onOpenConversation = {
                    navController.navigate(Screen.AIChat.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                userName = userName,
                userEmail = userEmail, // Pass userEmail to SettingsScreen
                securityViewModel = securityViewModel,
                onLogout = { authViewModel.signOut() },
                onProfileUpdate = onProfileUpdate,
                paddingValues = paddingValues
            )
        }

        // Main Tools
        composable(Screen.AIChat.route) {
            ChatScreen(viewModel = chatViewModel, paddingValues = paddingValues)
        }
        composable(Screen.TextWriter.route) {
            TextWriterScreen(viewModel = textWriterViewModel, paddingValues = paddingValues)
        }
        composable(Screen.CodeGenerator.route) {
            CodeGeneratorScreen(viewModel = codeGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.QuizGenerator.route) {
            QuizGeneratorScreen(viewModel = quizGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.ImageGenerator.route) {
            ImageGeneratorScreen(viewModel = imageGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.PPTGenerator.route) {
            PPTGeneratorScreen(viewModel = pptGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.PromptEngineer.route) {
            PromptEngineerScreen(viewModel = promptEngineerViewModel, paddingValues = paddingValues)
        }
        composable(Screen.DocumentAnalyzer.route) {
            DocumentAnalyzerScreen(viewModel = documentAnalyzerViewModel, paddingValues = paddingValues)
        }

        // Productivity Tools
        composable(Screen.PromptLibrary.route) {
            PromptLibraryScreen(paddingValues = paddingValues) { prompt ->
                // Navigate to AI Chat with the selected prompt
                chatViewModel.sendFromPromptLibrary(prompt)
                navController.navigate(Screen.AIChat.route)
            }
        }
        composable(Screen.StartupIdea.route) {
            StartupIdeaScreen(viewModel = startupIdeaViewModel, paddingValues = paddingValues)
        }
        composable(Screen.ResumeBuilder.route) {
            val resumeViewModel: ResumeViewModel = viewModel()
            ResumeBuilderScreen(viewModel = resumeViewModel, paddingValues = paddingValues)
        }

        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                paddingValues = paddingValues,
                onNavigate = { screen -> 
                    if (userRole == "admin") {
                        navController.navigate(screen.route) 
                    }
                },
                onLogout = { authViewModel.signOut() }
            )
        }
        
        // Admin Management
        composable(Screen.QuizManagement.route) { QuizManagementScreen(adminViewModel, paddingValues) }
        composable(Screen.FormulaManagement.route) { FormulaManagementScreen(adminViewModel, paddingValues) }
        composable(Screen.AnnouncementManagement.route) { AnnouncementManagementScreen(adminViewModel, paddingValues) }
        composable(Screen.ArticleManagement.route) { ArticleManagementScreen(adminViewModel, paddingValues) }

        // Student Tools Section
        composable(Screen.StudentTools.route) {
            StudentToolsScreen(paddingValues = paddingValues) { screen ->
                navController.navigate(screen.route)
            }
        }

        // Individual Student Tools
        composable(Screen.OfflineAssistant.route) {
            val offlineAssistantViewModel: OfflineAIAssistantViewModel = viewModel()
            OfflineAIAssistantScreen(viewModel = offlineAssistantViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Notes.route) {
            NotesScreen(viewModel = notesViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Flashcards.route) {
            FlashcardsScreen(viewModel = flashcardsViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Pomodoro.route) {
            PomodoroTimerScreen(viewModel = pomodoroViewModel, paddingValues = paddingValues)
        }
        composable(Screen.ToDo.route) {
            ToDoScreen(viewModel = toDoViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Calculator.route) {
            CalculatorScreen(viewModel = calculatorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.UnitConverter.route) {
            UnitConverterScreen(viewModel = unitConverterViewModel, paddingValues = paddingValues)
        }
        composable(Screen.OfflineQuiz.route) {
            QuizScreen(viewModel = offlineQuizViewModel, paddingValues = paddingValues)
        }
        composable(Screen.FormulaLibrary.route) {
            FormulaScreen(viewModel = formulaViewModel, paddingValues = paddingValues)
        }
        composable(Screen.MindMap.route) {
            MindMapScreen(viewModel = mindMapViewModel, paddingValues = paddingValues)
        }
        composable(Screen.StudyPlanner.route) {
            PlannerScreen(viewModel = studyPlannerViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Dictionary.route) { 
            DictionaryScreen(viewModel = dictionaryViewModel, paddingValues = paddingValues) 
        }
        composable(Screen.BookmarkManager.route) { 
            BookmarkManagerScreen(viewModel = bookmarkViewModel, paddingValues = paddingValues) 
        }
        composable(Screen.StudyArticles.route) {
            StudyArticlesScreen(viewModel = articlesViewModel, paddingValues = paddingValues)
        }

        composable(Screen.About.route) {
            AboutScreen(paddingValues = paddingValues)
        }
        
        composable(Screen.WordCounter.route) { TextToolsScreen(paddingValues = paddingValues) }
        composable(Screen.TextFormatter.route) { TextToolsScreen(paddingValues = paddingValues) }

        // ─── AI Learning Features ───
        composable(Screen.UploadStudyMaterial.route) {
            LaunchedEffect(Unit) { studyMaterialViewModel.resetForNewUpload() }
            UploadStudyMaterialScreen(
                viewModel = studyMaterialViewModel,
                paddingValues = paddingValues,
                onNavigateToMaterial = { materialId ->
                    studyMaterialViewModel.loadContentForMaterial(materialId)
                    navController.navigate(Screen.MaterialDetail.route)
                }
            )
        }
        composable(Screen.MaterialDetail.route) {
            MaterialDetailScreen(
                viewModel = studyMaterialViewModel,
                paddingValues = paddingValues,
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }
        composable(Screen.GeneratedQuestions.route) {
            GeneratedQuestionsScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.AIQuiz.route) {
            AIQuizScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.AIFlashcards.route) {
            AIFlashcardsScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.StudySummary.route) {
            StudySummaryScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.AITutor.route) {
            AITutorScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }

        // Admin Study Materials Management
        composable(Screen.AdminStudyMaterials.route) {
            AdminStudyMaterialsScreen(adminViewModel = adminViewModel, paddingValues = paddingValues)
        }

        composable(Screen.WebBrowser.route) {
            // When opened from the main app navigation, we hide the WebView's internal header
            // so the main top app bar remains and the drawer controls work as expected.
            WebBrowserScreen(url = "http://www.zayacodehub.online/", modifier = Modifier.fillMaxSize(), showHeader = false)
        }

        // ——— Brain Games ———
        composable(Screen.BrainGames.route) {
            BrainGamesScreen(paddingValues = paddingValues) { screen ->
                navController.navigate(screen.route)
            }
        }
        composable(Screen.MathChallenge.route) {
            MathChallengeScreen(paddingValues = paddingValues)
        }
        composable(Screen.MemoryMatrix.route) {
            MemoryMatrixScreen(paddingValues = paddingValues)
        }
        composable(Screen.WordScramble.route) {
            WordScrambleScreen(paddingValues = paddingValues)
        }
        composable(Screen.NumberSlider.route) {
            NumberSliderScreen(paddingValues = paddingValues)
        }
        composable(Screen.EmojiMatch.route) {
            EmojiMatchScreen(paddingValues = paddingValues)
        }
        composable(Screen.ColorRush.route) {
            ColorRushScreen(paddingValues = paddingValues)
        }
    }
}
