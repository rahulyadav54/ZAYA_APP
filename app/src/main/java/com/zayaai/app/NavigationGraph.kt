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
    userEmail: String,
    userRole: String,
    viewModelFactory: ViewModelFactory,
    onProfileUpdate: () -> Unit,
    onToolClick: (Tool) -> Unit
) {
    val context = LocalContext.current

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
                userEmail = userEmail,
                securityViewModel = securityViewModel,
                onLogout = { authViewModel.signOut() },
                onProfileUpdate = onProfileUpdate,
                paddingValues = paddingValues
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                innerPadding = paddingValues
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
            val codeGeneratorViewModel: CodeGeneratorViewModel = viewModel()
            CodeGeneratorScreen(viewModel = codeGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.QuizGenerator.route) {
            val quizGeneratorViewModel: QuizGeneratorViewModel = viewModel()
            QuizGeneratorScreen(viewModel = quizGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.ImageGenerator.route) {
            val imageGeneratorViewModel: ImageGeneratorViewModel = viewModel()
            ImageGeneratorScreen(viewModel = imageGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.PPTGenerator.route) {
            val pptGeneratorViewModel: PPTGeneratorViewModel = viewModel()
            PPTGeneratorScreen(viewModel = pptGeneratorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.PromptEngineer.route) {
            val promptEngineerViewModel: PromptEngineerViewModel = viewModel()
            PromptEngineerScreen(viewModel = promptEngineerViewModel, paddingValues = paddingValues)
        }
        composable(Screen.DocumentAnalyzer.route) {
            val documentAnalyzerViewModel: DocumentAnalyzerViewModel = viewModel()
            DocumentAnalyzerScreen(viewModel = documentAnalyzerViewModel, paddingValues = paddingValues)
        }

        // Productivity Tools
        composable(Screen.PromptLibrary.route) {
            PromptLibraryScreen(paddingValues = paddingValues) { prompt ->
                chatViewModel.sendFromPromptLibrary(prompt)
                navController.navigate(Screen.AIChat.route)
            }
        }
        composable(Screen.StartupIdea.route) {
            val startupIdeaViewModel: StartupIdeaViewModel = viewModel()
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
            val notesViewModel: NotesViewModel = viewModel(factory = viewModelFactory)
            NotesScreen(viewModel = notesViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Flashcards.route) {
            val flashcardsViewModel: FlashcardsViewModel = viewModel(factory = viewModelFactory)
            FlashcardsScreen(viewModel = flashcardsViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Pomodoro.route) {
            val pomodoroViewModel: PomodoroViewModel = viewModel()
            PomodoroTimerScreen(viewModel = pomodoroViewModel, paddingValues = paddingValues)
        }
        composable(Screen.ToDo.route) {
            val toDoViewModel: ToDoViewModel = viewModel(factory = viewModelFactory)
            ToDoScreen(viewModel = toDoViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Calculator.route) {
            val calculatorViewModel: CalculatorViewModel = viewModel()
            CalculatorScreen(viewModel = calculatorViewModel, paddingValues = paddingValues)
        }
        composable(Screen.UnitConverter.route) {
            val unitConverterViewModel: UnitConverterViewModel = viewModel()
            UnitConverterScreen(viewModel = unitConverterViewModel, paddingValues = paddingValues)
        }
        composable(Screen.OfflineQuiz.route) {
            val offlineQuizViewModel: QuizViewModel = viewModel(factory = viewModelFactory)
            QuizScreen(viewModel = offlineQuizViewModel, paddingValues = paddingValues)
        }
        composable(Screen.FormulaLibrary.route) {
            val formulaViewModel: FormulaViewModel = viewModel(factory = viewModelFactory)
            FormulaScreen(viewModel = formulaViewModel, paddingValues = paddingValues)
        }
        composable(Screen.MindMap.route) {
            val mindMapViewModel: MindMapViewModel = viewModel(factory = viewModelFactory)
            MindMapScreen(viewModel = mindMapViewModel, paddingValues = paddingValues)
        }
        composable(Screen.StudyPlanner.route) {
            val studyPlannerViewModel: StudyPlannerViewModel = viewModel(factory = viewModelFactory)
            PlannerScreen(viewModel = studyPlannerViewModel, paddingValues = paddingValues)
        }
        composable(Screen.Dictionary.route) { 
            val dictionaryViewModel: DictionaryViewModel = viewModel(factory = viewModelFactory)
            DictionaryScreen(viewModel = dictionaryViewModel, paddingValues = paddingValues) 
        }
        composable(Screen.BookmarkManager.route) { 
            val bookmarkViewModel: BookmarkViewModel = viewModel(factory = viewModelFactory)
            BookmarkManagerScreen(viewModel = bookmarkViewModel, paddingValues = paddingValues) 
        }
        composable(Screen.StudyArticles.route) {
            val articlesViewModel: StudyArticlesViewModel = viewModel(factory = viewModelFactory)
            StudyArticlesScreen(viewModel = articlesViewModel, paddingValues = paddingValues)
        }

        composable(Screen.About.route) {
            AboutScreen(paddingValues = paddingValues)
        }
        
        composable(Screen.WordCounter.route) { TextToolsScreen(paddingValues = paddingValues) }
        composable(Screen.TextFormatter.route) { TextToolsScreen(paddingValues = paddingValues) }

        // ─── AI Learning Features ───
        composable(Screen.UploadStudyMaterial.route) {
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
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
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
            MaterialDetailScreen(
                viewModel = studyMaterialViewModel,
                paddingValues = paddingValues,
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }
        composable(Screen.GeneratedQuestions.route) {
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
            GeneratedQuestionsScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.AIQuiz.route) {
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
            AIQuizScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.AIFlashcards.route) {
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
            AIFlashcardsScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.StudySummary.route) {
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
            StudySummaryScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }
        composable(Screen.AITutor.route) {
            val db = remember(context) { AiZayaDatabase.getDatabase(context) }
            val repo = remember(db) { AiZayaRepository(db) }
            val studyMaterialViewModelFactory = remember(repo, db) { StudyMaterialViewModelFactory(repo, db) }
            val studyMaterialViewModel: StudyMaterialViewModel = viewModel(factory = studyMaterialViewModelFactory)
            AITutorScreen(viewModel = studyMaterialViewModel, paddingValues = paddingValues)
        }

        // Admin Study Materials Management
        composable(Screen.AdminStudyMaterials.route) {
            AdminStudyMaterialsScreen(adminViewModel = adminViewModel, paddingValues = paddingValues)
        }

        composable(Screen.WebBrowser.route) {
            WebBrowserScreen(url = "http://www.zayacodehub.online/", modifier = Modifier.fillMaxSize(), showHeader = false)
        }

        // ——— Brain Games ———
        composable(Screen.BrainGames.route) {
            BrainGamesScreen(paddingValues = paddingValues) { screen ->
                navController.navigate(screen.route)
            }
        }
        composable(Screen.EventsHub.route) {
            val eventsViewModel: com.zayaai.app.events.EventsViewModel = viewModel()
            com.zayaai.app.events.EventsHubScreen(
                viewModel = eventsViewModel,
                onBack = { navController.popBackStack() }
            )
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
