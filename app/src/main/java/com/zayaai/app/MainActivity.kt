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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zayaai.app.ui.theme.*
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

                                MainAppScaffold(
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
fun MainAppScaffold(
    authViewModel: AuthViewModel,
    userRole: String,
    viewModelFactory: ViewModelFactory,
    securityViewModel: SecurityViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
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
                drawerContainerColor = if (LocalIsDarkTheme.current) Color(0xFF0B0C16) else Color(0xFFF8F9FE),
                drawerTonalElevation = 0.dp,
                modifier = Modifier.width(320.dp)
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
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryPurple)
    }
}

// ─── 5-Tab Bottom Bar Definition (Matches Reference Mockup) ───
data class RedesignBottomTab(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val isCenterButton: Boolean = false
)

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
    val isDark = LocalIsDarkTheme.current

    // Bottom Navigation Tabs (Reference Mockup: Home, Tools, Chat, History, Profile)
    val bottomTabs = remember {
        listOf(
            RedesignBottomTab("Home", Icons.Rounded.Home, Screen.Home.route),
            RedesignBottomTab("Tools", Icons.Rounded.GridView, Screen.StudentTools.route),
            RedesignBottomTab("Voice", Icons.Rounded.Mic, Screen.Voice.route, isCenterButton = true),
            RedesignBottomTab("History", Icons.Rounded.History, Screen.History.route),
            RedesignBottomTab("Profile", Icons.Rounded.Person, Screen.Settings.route)
        )
    }

    val showBottomBar = currentRoute in bottomTabs.map { it.route } ||
            currentRoute == Screen.StudentTools.route ||
            currentRoute == Screen.Voice.route ||
            currentRoute == Screen.AIChat.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground)
    ) {
        Scaffold(
            topBar = {
                // Top App Bar directly matching reference image
                RedesignTopBar(
                    currentRoute = currentRoute,
                    onOpenDrawer = onOpenDrawer,
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = {
                        // Navigate to Announcements / History
                        navController.navigate(Screen.History.route)
                    }
                )
            },
            bottomBar = {
                if (showBottomBar && userRole != "admin") {
                    RedesignBottomBar(
                        tabs = bottomTabs,
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
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
                    val route = when(tool.title.trim()) {
                        "AI Chat" -> Screen.AIChat.route
                        "Text Writer" -> Screen.TextWriter.route
                        "Image Gen" -> Screen.ImageGenerator.route
                        "Code Gen" -> Screen.CodeGenerator.route
                        "Quiz Gen" -> Screen.QuizGenerator.route
                        "Doc Analyzer" -> Screen.DocumentAnalyzer.route
                        "Student Tools" -> Screen.StudentTools.route
                        "Scientific Calculator" -> Screen.Calculator.route
                        "Unit Converter" -> Screen.UnitConverter.route
                        "Word Counter" -> Screen.WordCounter.route
                        "Dictionary" -> Screen.Dictionary.route
                        "Study Planner" -> Screen.StudyPlanner.route
                        "Notes" -> Screen.Notes.route
                        "To-Do List" -> Screen.ToDo.route
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

// ─── Top App Bar Matching Reference Mockup ───
@Composable
private fun RedesignTopBar(
    currentRoute: String?,
    onOpenDrawer: () -> Unit,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val isHome = currentRoute == Screen.Home.route || currentRoute == null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Icon: Hamburger Menu on Home, Back Arrow on Sub-screens
        IconButton(
            onClick = if (isHome) onOpenDrawer else onBackClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = if (isHome) Icons.Rounded.Menu else Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = if (isHome) "Menu" else "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        // Center: AI ZAYA Title + 👑 Pro Pill Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "AI ZAYA",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // 👑 Pro Badge Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF231C38) else Color(0xFFF3E8FF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) ProGold.copy(alpha = 0.4f) else PrimaryPurple.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("👑", fontSize = 11.sp)
                    Text(
                        text = "Pro",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ProGold else PrimaryPurple
                    )
                }
            }
        }

        // Right Icon: Notification Bell
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsNone,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─── 5-Item Modern Bottom Navigation Bar With Elevated Center Voice Assistant ───
@Composable
private fun RedesignBottomBar(
    tabs: List<RedesignBottomTab>,
    currentRoute: String?,
    onTabSelected: (RedesignBottomTab) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = if (isDark) Color(0xFF101122) else Color(0xFFFFFFFF),
        shadowElevation = if (isDark) 0.dp else 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) DarkBorder else LightBorder
        ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = when (tab.route) {
                    Screen.Home.route -> currentRoute == Screen.Home.route || currentRoute == null
                    Screen.StudentTools.route -> currentRoute == Screen.StudentTools.route
                    Screen.Voice.route -> currentRoute == Screen.Voice.route
                    Screen.History.route -> currentRoute == Screen.History.route
                    Screen.Settings.route -> currentRoute == Screen.Settings.route
                    else -> currentRoute == tab.route
                }

                if (tab.isCenterButton) {
                    RedesignCenterVoiceButton(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) }
                    )
                } else {
                    RedesignBottomNavItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RedesignCenterVoiceButton(
    tab: RedesignBottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        label = "voice_scale"
    )

    Column(
        modifier = Modifier
            .offset(y = (-6).dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Elevated Glowing Mic Button (Slightly larger, prominent in the center)
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = if (isSelected) 10.dp else 6.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF7C3AED).copy(alpha = if (isSelected) 0.7f else 0.45f)
                )
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF38BDF8))
                    ),
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 2.5.dp else 2.dp,
                    color = if (isSelected) Color(0xFF38BDF8) else if (isDark) Color(0xFF1E1F35) else Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = tab.title,
            color = if (isSelected) Color(0xFF7C3AED) else if (isDark) DarkTextSecondary else LightTextSecondary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun RedesignBottomNavItem(
    tab: RedesignBottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) DarkBottomBarSelected else LightBottomBarSelected
        } else {
            Color.Transparent
        },
        label = "nav_pill"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) DarkBottomBarSelectedIcon else LightBottomBarSelectedIcon
        } else {
            if (isDark) DarkTextSecondary else LightTextSecondary
        },
        label = "nav_content"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        label = "nav_scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with Pill Container when selected
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 30.dp)
                .background(pillColor, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label
        Text(
            text = tab.title,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.5.sp
            ),
            maxLines = 1
        )
    }
}
