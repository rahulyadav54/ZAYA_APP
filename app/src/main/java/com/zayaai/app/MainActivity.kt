package com.zayaai.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zayaai.app.ui.theme.AIZAYATheme
import com.zayaai.app.ui.theme.ThemeMode
import com.zayaai.app.ui.theme.NeonPurple
import com.zayaai.app.ui.theme.NeonCyan
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            // Splash screen state
            var showSplash by remember { mutableStateOf(true) }
            
            if (showSplash) {
                SplashScreen(onSplashComplete = { showSplash = false })
            } else {
                // Lazy initialization of ViewModels and repositories
                val authManager = remember { AuthManager(context) }
                val userRepository = remember { UserRepository() }
                val dataStoreManager = remember { DataStoreManager(context) }
                val authRepository = remember { AuthRepository(authManager, userRepository) }
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository, dataStoreManager))
                
                val securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModelFactory(dataStoreManager, context))
                
                val currentUser = authViewModel.currentUser
                val userRole = authViewModel.userRole
                val firestoreUserEmail = authViewModel.firestoreUserEmail
                val settings by securityViewModel.settings.collectAsState()
                val isUnlocked = securityViewModel.isUnlocked

                val themeMode = try {
                    ThemeMode.valueOf(settings.themeMode.replaceFirstChar { it.uppercase() })
                } catch (_: Exception) {
                    ThemeMode.Light
                }

                AIZAYATheme(themeMode = themeMode) {
                    Crossfade(targetState = Triple(currentUser, userRole, firestoreUserEmail), label = "main_fade") { (user, role, fsEmail) ->
                        if (user == null && fsEmail == null) {
                            var currentAuthScreen by remember { mutableStateOf(AuthScreen.Login) }
                            when (currentAuthScreen) {
                                AuthScreen.Login -> LoginScreen(
                                    authViewModel = authViewModel,
                                    onLoginSuccess = { /* Managed by AuthViewModel state */ },
                                    onNavigateToRegister = { currentAuthScreen = AuthScreen.Register },
                                    onNavigateToForgotPassword = { currentAuthScreen = AuthScreen.ForgotPassword }
                                )
                                AuthScreen.Register -> RegistrationScreen(
                                    authViewModel = authViewModel,
                                    onRegisterSuccess = { currentAuthScreen = AuthScreen.Login },
                                    onNavigateToLogin = { currentAuthScreen = AuthScreen.Login }
                                )
                                AuthScreen.ForgotPassword -> ForgotPasswordScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { currentAuthScreen = AuthScreen.Login }
                                )
                            }
                        } else {
                            if (role == null) {
                                LaunchedEffect(user) {
                                    authViewModel.checkUserSessionIfNeeded()
                                }
                                LoadingScreen()
                            } else if (settings.appLockEnabled && !isUnlocked.value) {
                                AppLockScreen(securityViewModel = securityViewModel, onUnlock = { securityViewModel.setUnlocked(true) })
                            } else {
                                val database = remember { AiZayaDatabase.getDatabase(context) }
                                val repository = remember { AiZayaRepository(database) }
                                val viewModelFactory = remember { ViewModelFactory(repository) }
                                
                                AuthenticatedApp(
                                    authViewModel = authViewModel,
                                    userRole = role,
                                    viewModelFactory = viewModelFactory,
                                    securityViewModel = securityViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticatedApp(
    authViewModel: AuthViewModel,
    userRole: String,
    viewModelFactory: ViewModelFactory,
    securityViewModel: SecurityViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    
    val chatViewModel: ChatViewModel = viewModel()
    val textWriterViewModel: TextWriterViewModel = viewModel()
    val voiceViewModel: VoiceViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return VoiceViewModel(context, AiZayaRepository(AiZayaDatabase.getDatabase(context))) as T
        }
    })
    val adminViewModel: AdminViewModel = viewModel()

    val userEmail = authViewModel.currentUser?.email ?: authViewModel.firestoreUserEmail ?: ""

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerTonalElevation = 0.dp
            ) {
                DrawerContent(
                    userName = authViewModel.displayName,
                    userEmail = userEmail,
                    userRole = userRole,
                    currentRoute = currentRoute,
                    onItemSelected = { screen ->
                        scope.launch { drawerState.close() }
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        authViewModel.signOut()
                    }
                )
            }
        }
    ) {
        MainScreenContent(
            userName = authViewModel.displayName,
            userEmail = userEmail,
            userRole = userRole,
            navController = navController,
            currentRoute = currentRoute,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            onSignOut = {
                authViewModel.signOut()
            },
            onProfileUpdate = { 
                authViewModel.checkUserSessionIfNeeded()
            },
            chatViewModel = chatViewModel,
            textWriterViewModel = textWriterViewModel,
            voiceViewModel = voiceViewModel,
            securityViewModel = securityViewModel,
            adminViewModel = adminViewModel,
            authViewModel = authViewModel,
            viewModelFactory = viewModelFactory
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F1A)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF00E5FF))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    userName: String,
    userEmail: String,
    userRole: String,
    navController: androidx.navigation.NavHostController,
    currentRoute: String?,
    onOpenDrawer: () -> Unit,
    onSignOut: () -> Unit,
    onProfileUpdate: () -> Unit,
    chatViewModel: ChatViewModel,
    textWriterViewModel: TextWriterViewModel,
    voiceViewModel: VoiceViewModel,
    securityViewModel: SecurityViewModel,
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    viewModelFactory: ViewModelFactory
) {
    val bottomBarScreens = if (userRole == "admin") emptyList() else listOf(Screen.Home, Screen.Voice, Screen.History, Screen.Settings)
    val showBottomBar = currentRoute in bottomBarScreens.map { it.route }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    // use theme-aware background colors so light/dark both look correct
                    colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
                )
            )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when(currentRoute) {
                                Screen.AIChat.route -> "AI Chat"
                                Screen.Voice.route -> "Voice Assistant"
                                Screen.TextWriter.route -> "Text Writer"
                                Screen.StudentTools.route -> "Student Tools"
                                Screen.AdminDashboard.route -> "Admin Dashboard"
                                Screen.WebBrowser.route -> "ZAYA EDU"
                                else -> "AI ZAYA"
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        if (currentRoute == Screen.Home.route || currentRoute == Screen.AdminDashboard.route) {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        } else {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    },
                    actions = {
                        if (currentRoute == Screen.Home.route || currentRoute == Screen.AdminDashboard.route) {
                            IconButton(onClick = onSignOut) {
                                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    ProfessionalBottomBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        screens = bottomBarScreens
                    )
                }
            },
            floatingActionButton = {
                if (currentRoute == Screen.Home.route && userRole != "admin") {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.AIChat.route) },
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp),
                        modifier = Modifier.size(70.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan)), CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "AI Chat", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavigationGraph(
                navController = navController,
                paddingValues = innerPadding,
                chatViewModel = chatViewModel,
                textWriterViewModel = textWriterViewModel,
                voiceViewModel = voiceViewModel,
                securityViewModel = securityViewModel,
                adminViewModel = adminViewModel,
                authViewModel = authViewModel,
                userName = userName,
                userEmail = userEmail,
                userRole = userRole,
                viewModelFactory = viewModelFactory,
                onProfileUpdate = onProfileUpdate,
                onToolClick = { tool ->
                    val route = when(tool.title) {
                        "AI Chat" -> Screen.AIChat.route
                        "Text Writer" -> Screen.TextWriter.route
                        "Image Gen" -> Screen.ImageGenerator.route
                        "Code Gen" -> Screen.CodeGenerator.route
                        "Quiz Gen" -> Screen.QuizGenerator.route
                        "Student Tools" -> Screen.StudentTools.route
                        "Zaya Edu" -> Screen.WebBrowser.route
                        "Brain Games" -> Screen.BrainGames.route
                        "Startup Idea" -> Screen.StartupIdea.route
                        "Prompt Library" -> Screen.PromptLibrary.route
                        "Prompt Engineer" -> Screen.PromptEngineer.route
                        "AI Study" -> Screen.UploadStudyMaterial.route
                        "PPT Gen" -> Screen.PPTGenerator.route
                        "Resume Builder" -> Screen.ResumeBuilder.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(route)
                }
            )
        }
    }
}

@Composable
fun ProfessionalBottomBar(
    navController: androidx.navigation.NavHostController,
    currentRoute: String?,
    screens: List<Screen>
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isSelected = currentRoute == screen.route
                BottomNavItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        label = "scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Text(
                text = screen.title,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .background(contentColor, CircleShape)
            )
        }
    }
}
