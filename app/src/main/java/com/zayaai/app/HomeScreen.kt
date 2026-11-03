package com.zayaai.app

import androidx.compose.runtime.DisposableEffect
import java.util.Calendar
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ads.BannerAdCard
import com.zayaai.app.ui.theme.*

// ─── Tool Item Model for 3-Column Grid ───
@Immutable
data class RedesignToolItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val category: String,
    val gradientColors: List<Color>,
    val route: String,
    val brush: Brush = Brush.linearGradient(gradientColors)
)


fun getTimeBasedGreeting(name: String): Pair<String, String> {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val cleanName = if (name.isBlank() || name.equals("User", ignoreCase = true) || name.equals("Rahul", ignoreCase = true)) {
        val fb = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        fb?.displayName?.takeIf { it.isNotBlank() }
            ?: fb?.email?.substringBefore("@")?.replaceFirstChar { it.uppercaseChar() }
            ?: "Explorer"
    } else {
        name
    }
    val firstName = cleanName.trim().split(" ").firstOrNull()?.replaceFirstChar { it.uppercaseChar() } ?: "Explorer"

    return when (hour) {
        in 5..11 -> Pair("Good Morning, $firstName! ☀️", "Ready to explore something new today?")
        in 12..16 -> Pair("Good Afternoon, $firstName! 🌤️", "What would you like to accomplish today?")
        in 17..20 -> Pair("Good Evening, $firstName! 🌆", "Let's review your progress and explore new ideas.")
        else -> Pair("Good Night, $firstName! 🌙", "Wrapping up the day? Feel free to explore anytime.")
    }
}

@Composable
fun HomeScreen(
    userName: String,
    innerPadding: PaddingValues,
    onToolClick: (Tool) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    // Clean display name (never hardcode dummy name)
    val displayName = remember(userName) {
        if (userName.isBlank() || userName.equals("User", ignoreCase = true) || userName.equals("Rahul", ignoreCase = true)) {
            val fb = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            fb?.displayName?.takeIf { it.isNotBlank() }
                ?: fb?.email?.substringBefore("@")?.replaceFirstChar { it.uppercaseChar() }
                ?: "Explorer"
        } else {
            userName
        }
    }

    // Dynamic Time-Based Greeting with Lifecycle Resume auto-refresh
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var greetingState by remember { mutableStateOf(getTimeBasedGreeting(displayName)) }

    DisposableEffect(lifecycleOwner, displayName) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                greetingState = getTimeBasedGreeting(displayName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Selected Category Filter Pill
    var selectedCategory by remember { mutableStateOf("All Tools") }
    val categories = remember { listOf("All Tools", "Student", "Productivity", "Creative") }

    // Master Catalog of Redesigned Tools
    val allTools = remember {
        listOf(
            // AI Tools
            RedesignToolItem("ai_chat", "AI Chat", Icons.AutoMirrored.Rounded.Chat, "AI Tools", listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)), Screen.AIChat.route),
            RedesignToolItem("image_gen", "Image Gen", Icons.Rounded.Image, "AI Tools", listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), Screen.ImageGenerator.route),
            RedesignToolItem("code_gen", "Code Gen", Icons.Rounded.Code, "AI Tools", listOf(Color(0xFF38BDF8), Color(0xFF6366F1)), Screen.CodeGenerator.route),
            RedesignToolItem("text_writer", "Text Writer", Icons.Rounded.EditNote, "AI Tools", listOf(Color(0xFFA855F7), Color(0xFFEC4899)), Screen.TextWriter.route),
            RedesignToolItem("quiz_gen", "Quiz Gen", Icons.Rounded.Quiz, "AI Tools", listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), Screen.QuizGenerator.route),
            RedesignToolItem("doc_analyzer", "Doc Analyzer", Icons.Rounded.FindInPage, "AI Tools", listOf(Color(0xFF6366F1), Color(0xFF3B82F6)), Screen.DocumentAnalyzer.route),

            // Student Tools
            RedesignToolItem("calculator", "Scientific\nCalculator", Icons.Rounded.Calculate, "Student", listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)), Screen.Calculator.route),
            RedesignToolItem("unit_converter", "Unit\nConverter", Icons.Rounded.SwapHoriz, "Student", listOf(Color(0xFF10B981), Color(0xFF059669)), Screen.UnitConverter.route),
            RedesignToolItem("word_counter", "Word\nCounter", Icons.Rounded.TextFields, "Student", listOf(Color(0xFF3B82F6), Color(0xFF2563EB)), Screen.WordCounter.route),
            RedesignToolItem("dictionary", "Dictionary", Icons.AutoMirrored.Rounded.MenuBook, "Student", listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)), Screen.Dictionary.route),
            RedesignToolItem("study_planner", "Study\nPlanner", Icons.Rounded.CalendarMonth, "Student", listOf(Color(0xFFF59E0B), Color(0xFFD97706)), Screen.StudyPlanner.route),
            RedesignToolItem("notes", "Notes", Icons.Rounded.Description, "Student", listOf(Color(0xFFEC4899), Color(0xFFBE185D)), Screen.Notes.route),

            // Productivity Tools
            RedesignToolItem("todo", "To-Do List", Icons.Rounded.Checklist, "Productivity", listOf(Color(0xFF10B981), Color(0xFF047857)), Screen.ToDo.route),
            RedesignToolItem("prompt_lib", "Prompt\nLibrary", Icons.Rounded.LibraryBooks, "Productivity", listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)), Screen.PromptLibrary.route),
            RedesignToolItem("resume_builder", "Resume\nBuilder", Icons.Rounded.ContactPage, "Productivity", listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)), Screen.ResumeBuilder.route),
            RedesignToolItem("startup_idea", "Startup Idea", Icons.Rounded.Lightbulb, "Productivity", listOf(Color(0xFFF59E0B), Color(0xFFB45309)), Screen.StartupIdea.route),
            RedesignToolItem("ppt_gen", "PPT Gen", Icons.Rounded.Slideshow, "Productivity", listOf(Color(0xFF06B6D4), Color(0xFF0E7490)), Screen.PPTGenerator.route),
            RedesignToolItem("brain_games", "Brain Games", Icons.Rounded.SportsEsports, "Creative", listOf(Color(0xFFEC4899), Color(0xFF9333EA)), Screen.BrainGames.route),
            RedesignToolItem("events_hub", "Events Hub", Icons.Rounded.Event, "Creative", listOf(Color(0xFF7C3AED), Color(0xFF38BDF8)), Screen.EventsHub.route)
        )
    }

    // Pre-chunked rows for ultra-efficient lazy recycling
    val aiToolRows = remember(allTools) {
        allTools.filter { it.category == "AI Tools" }.chunked(3)
    }
    val studentToolRows = remember(allTools) {
        allTools.filter { it.category == "Student" }.chunked(3)
    }
    val productivityToolRows = remember(allTools) {
        allTools.filter { it.category == "Productivity" }.chunked(3)
    }
    val creativeToolRows = remember(allTools) {
        allTools.filter { it.category == "Creative" || it.id in listOf("image_gen", "text_writer", "startup_idea") }.take(6).chunked(3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── 1. Greeting Header ───
        item(key = "home_header") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = greetingState.first,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = greetingState.second,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ─── 2. Animated Auto-Sliding Promotional Hero Carousel ───
        item(key = "home_hero_carousel") {
            HomeHeroCarousel(
                onEventsHubClick = {
                    onToolClick(Tool("Events Hub", "Explore hackathons and tech events", Icons.Rounded.Event, PrimaryPurple))
                },
                onAIChatClick = {
                    onToolClick(Tool("AI Chat", "Advanced conversational AI", Icons.AutoMirrored.Rounded.Chat, PrimaryPurple))
                }
            )
        }

        // ─── 3. Category Filter Pills ───
        item(key = "home_category_pills") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (isSelected) PrimaryPurple else if (isDark) DarkPillInactive else LightPillInactive,
                        border = if (!isSelected) {
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDark) DarkBorder else LightBorder
                            )
                        } else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) Color.White else if (isDark) DarkPillInactiveText else LightPillInactiveText
                        )
                    }
                }
            }
        }

        // ─── 4. Tool Sections (Chunked rows with LazyColumn recycling) ───
        if (selectedCategory == "All Tools" || selectedCategory == "AI Tools") {
            item(key = "sec_header_ai") {
                SectionHeader(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "AI Tools",
                    onSeeAll = {
                        onToolClick(Tool("AI Chat", "Advanced conversational AI", Icons.AutoMirrored.Rounded.Chat, PrimaryPurple))
                    }
                )
            }
            items(
                items = aiToolRows,
                key = { row -> "ai_row_" + row.joinToString("-") { it.id } },
                contentType = { "tool_row" }
            ) { rowTools ->
                ToolGridRow(rowTools = rowTools, onToolClick = onToolClick)
            }
        }

        if (selectedCategory == "All Tools" || selectedCategory == "Student") {
            item(key = "sec_header_student") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    icon = Icons.Rounded.School,
                    title = "Student Tools",
                    onSeeAll = {
                        onToolClick(Tool("Student Tools", "Academic success toolkit", Icons.Rounded.School, PrimaryPurple))
                    }
                )
            }
            items(
                items = studentToolRows,
                key = { row -> "student_row_" + row.joinToString("-") { it.id } },
                contentType = { "tool_row" }
            ) { rowTools ->
                ToolGridRow(rowTools = rowTools, onToolClick = onToolClick)
            }
        }

        if (selectedCategory == "All Tools" || selectedCategory == "Productivity") {
            item(key = "sec_header_productivity") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    icon = Icons.Rounded.Bolt,
                    title = "Productivity Tools",
                    onSeeAll = {
                        onToolClick(Tool("Prompt Library", "Ready-to-use AI prompts", Icons.Rounded.LibraryBooks, PrimaryPurple))
                    }
                )
            }
            items(
                items = productivityToolRows,
                key = { row -> "prod_row_" + row.joinToString("-") { it.id } },
                contentType = { "tool_row" }
            ) { rowTools ->
                ToolGridRow(rowTools = rowTools, onToolClick = onToolClick)
            }
        }

        if (selectedCategory == "All Tools" || selectedCategory == "Creative") {
            item(key = "sec_header_creative") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    icon = Icons.Rounded.Palette,
                    title = "Creative Tools",
                    onSeeAll = {
                        onToolClick(Tool("Image Gen", "Create art from text", Icons.Rounded.Image, PrimaryPurple))
                    }
                )
            }
            items(
                items = creativeToolRows,
                key = { row -> "creative_row_" + row.joinToString("-") { it.id } },
                contentType = { "tool_row" }
            ) { rowTools ->
                ToolGridRow(rowTools = rowTools, onToolClick = onToolClick)
            }
        }

        // ─── 5. Sponsored Banner Ad ───
        item(key = "home_banner_ad") {
            Spacer(modifier = Modifier.height(8.dp))
            BannerAdCard()
        }

        // Extra Bottom Spacing for Floating / Elevated Navigation Bar
        item(key = "home_bottom_spacer") {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ─── Animated Auto-Sliding Promotional Hero Carousel ───
@Composable
private fun HomeHeroCarousel(
    onEventsHubClick: () -> Unit,
    onAIChatClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val scope = rememberCoroutineScope()

    // Respect Android accessibility and reduced-motion preferences
    val isReducedMotion = remember(context) {
        try {
            val animDuration = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            val transDuration = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            )
            animDuration == 0f || transDuration == 0f
        } catch (_: Exception) {
            false
        }
    }

    // Lifecycle observer to prevent leaks and pause autoplay when app is paused/backgrounded
    var isAppResumed by remember { mutableStateOf(true) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAppResumed = true
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                isAppResumed = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Autoplay Timer (4.5 seconds): auto-pauses when user drags, when reduced motion is preferred, or when app is in background
    LaunchedEffect(isDragged, isAppResumed, isReducedMotion) {
        if (!isReducedMotion && isAppResumed && !isDragged) {
            while (true) {
                kotlinx.coroutines.delay(4500)
                val nextPage = (pagerState.currentPage + 1) % 2
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
        ) { page ->
            if (page == 0) {
                EventsHubBannerSlide(
                    isDark = isDark,
                    onSlideClick = onEventsHubClick
                )
            } else {
                AIChatBannerSlide(
                    isDark = isDark,
                    onSlideClick = onAIChatClick
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated Pagination Indicators with Tap-to-jump support
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 7.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "indicator_width"
                )
                val indicatorColor by animateColorAsState(
                    targetValue = if (isSelected) PrimaryPurple else if (isDark) Color(0xFF333652) else Color(0xFFCBD5E1),
                    animationSpec = tween(durationMillis = 300),
                    label = "indicator_color"
                )

                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(width)
                        .clip(RoundedCornerShape(3.dp))
                        .background(indicatorColor)
                        .clickable {
                            scope.launch {
                                if (isReducedMotion) {
                                    pagerState.scrollToPage(index)
                                } else {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        }
                )
            }
        }
    }
}

// ─── Slide 1: Events Hub Banner ───
@Composable
private fun EventsHubBannerSlide(
    isDark: Boolean,
    onSlideClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(22.dp)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onSlideClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1E113C),
                                Color(0xFF16143A),
                                Color(0xFF0F172A)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF4F46E5),
                                Color(0xFF7C3AED),
                                Color(0xFF9333EA)
                            )
                        )
                    }
                )
                .then(
                    if (isDark) {
                        Modifier.border(
                            1.dp,
                            Color(0xFF38BDF8).copy(alpha = 0.25f),
                            RoundedCornerShape(22.dp)
                        )
                    } else Modifier
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Left Content: Heading, Subtitle & CTA Button
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.68f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Discover. Participate. Innovate.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Explore upcoming hackathons, tech events, workshops and competitions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        ),
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // CTA Button: Explore Events →
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color.White,
                    border = if (isDark) BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)) else null,
                    modifier = Modifier.clip(RoundedCornerShape(50.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Explore Events",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Right Graphic: 3D Holographic Events / Trophy Graphic
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                FuturisticEventsGraphic()
            }
        }
    }
}

// ─── Slide 2: AI Chat Banner ───
@Composable
private fun AIChatBannerSlide(
    isDark: Boolean,
    onSlideClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(22.dp)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onSlideClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF26194A),
                                Color(0xFF1B1137),
                                Color(0xFF120C26)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF7C3AED),
                                Color(0xFF6D28D9),
                                Color(0xFF4F46E5)
                            )
                        )
                    }
                )
                .then(
                    if (isDark) {
                        Modifier.border(
                            1.dp,
                            Color(0xFF8B5CF6).copy(alpha = 0.25f),
                            RoundedCornerShape(22.dp)
                        )
                    } else Modifier
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Left Content: Heading, Subtitle & CTA Button
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.68f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Your AI. Your Possibilities.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Chat, code, create, write and explore with AI ZAYA.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        ),
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // CTA Button: Start Chatting →
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.20f) else Color.White,
                    border = if (isDark) BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)) else null,
                    modifier = Modifier.clip(RoundedCornerShape(50.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Start Chatting",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (isDark) Color.White else PrimaryPurple
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) Color.White else PrimaryPurple,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Right Graphic: Sleek Futuristic 3D Assistant Robot Character
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                FuturisticRobotGraphic()
            }
        }
    }
}

// ─── Procedural Vector Graphic: Futuristic Events & Hackathon Trophy ───
@Composable
private fun FuturisticEventsGraphic() {
    Spacer(
        modifier = Modifier
            .size(100.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height

                val auraBrush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.40f), Color(0xFF7C3AED).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.52f
                )
                val trophyBrush = Brush.verticalGradient(
                    listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706))
                )
                val platformBrush = Brush.verticalGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF1E293B))
                )
                val cyberCyan = Color(0xFF38BDF8)
                val cyberPurple = Color(0xFFA855F7)

                onDrawBehind {
                    // 1. Holographic Aura Glow
                    drawCircle(brush = auraBrush, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.5f)

                    // 2. Holographic Elliptical Platform Base
                    drawOval(
                        brush = platformBrush,
                        topLeft = Offset(w * 0.18f, h * 0.72f),
                        size = Size(w * 0.64f, h * 0.22f)
                    )
                    drawOval(
                        color = cyberCyan.copy(alpha = 0.8f),
                        topLeft = Offset(w * 0.18f, h * 0.72f),
                        size = Size(w * 0.64f, h * 0.22f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                    )

                    // 3. Cyber Light Beam Upward
                    drawOval(
                        color = cyberCyan.copy(alpha = 0.2f),
                        topLeft = Offset(w * 0.28f, h * 0.68f),
                        size = Size(w * 0.44f, h * 0.15f)
                    )

                    // 4. Tech Trophy Base
                    drawRoundRect(
                        brush = trophyBrush,
                        topLeft = Offset(w * 0.44f, h * 0.58f),
                        size = Size(w * 0.12f, h * 0.16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, h * 0.04f)
                    )

                    // Trophy Cup Body
                    val cupPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.32f, h * 0.28f)
                        lineTo(w * 0.68f, h * 0.28f)
                        lineTo(w * 0.60f, h * 0.58f)
                        lineTo(w * 0.40f, h * 0.58f)
                        close()
                    }
                    drawPath(path = cupPath, brush = trophyBrush)

                    // Trophy Handles
                    drawArc(
                        color = Color(0xFFF59E0B),
                        startAngle = 120f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.22f, h * 0.32f),
                        size = Size(w * 0.18f, h * 0.20f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                    )
                    drawArc(
                        color = Color(0xFFF59E0B),
                        startAngle = -60f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.60f, h * 0.32f),
                        size = Size(w * 0.18f, h * 0.20f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                    )

                    // 5. Floating Glowing Star Emblem on the Trophy
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.05f,
                        center = Offset(w * 0.5f, h * 0.42f)
                    )
                    drawCircle(
                        color = cyberCyan,
                        radius = w * 0.03f,
                        center = Offset(w * 0.5f, h * 0.42f)
                    )

                    // 6. Holographic Orbit Rings
                    drawArc(
                        color = cyberCyan.copy(alpha = 0.6f),
                        startAngle = -30f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.12f, h * 0.22f),
                        size = Size(w * 0.76f, h * 0.40f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    // 7. Floating Tech Sparks / Energy Dots
                    drawCircle(color = Color(0xFFFDE047), radius = 2.5.dp.toPx(), center = Offset(w * 0.26f, h * 0.24f))
                    drawCircle(color = cyberCyan, radius = 2.5.dp.toPx(), center = Offset(w * 0.76f, h * 0.22f))
                    drawCircle(color = cyberPurple, radius = 2.dp.toPx(), center = Offset(w * 0.34f, h * 0.64f))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(w * 0.68f, h * 0.62f))
                }
            }
    )
}

/**
 * Procedural Vector Graphic representing 3D Robot Assistant.
 * Uses drawWithCache to completely avoid allocations during scrolling.
 */
@Composable
private fun FuturisticRobotGraphic() {
    Spacer(
        modifier = Modifier
            .size(100.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height

                val auraBrush = Brush.radialGradient(
                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.5f
                )
                val headBrush = Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1), Color(0xFF94A3B8))
                )
                val visorBrush = Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
                val bodyBrush = Brush.verticalGradient(
                    listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
                )

                onDrawBehind {
                    // Outer Glow Aura
                    drawCircle(brush = auraBrush, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.5f)

                    // Head Base (Rounded Helmet)
                    drawRoundRect(
                        brush = headBrush,
                        topLeft = Offset(w * 0.18f, h * 0.18f),
                        size = Size(w * 0.64f, h * 0.52f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.22f, h * 0.22f)
                    )

                    // Visor (Glossy Dark Display Screen)
                    drawRoundRect(
                        brush = visorBrush,
                        topLeft = Offset(w * 0.25f, h * 0.28f),
                        size = Size(w * 0.50f, h * 0.30f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, h * 0.12f)
                    )

                    // Visor Border
                    drawRoundRect(
                        color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.25f, h * 0.28f),
                        size = Size(w * 0.50f, h * 0.30f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, h * 0.12f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    // Eyes
                    drawOval(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.35f, h * 0.38f),
                        size = Size(w * 0.08f, h * 0.09f)
                    )
                    drawOval(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.57f, h * 0.38f),
                        size = Size(w * 0.08f, h * 0.09f)
                    )

                    // Headphone Ears
                    drawOval(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.12f, h * 0.35f),
                        size = Size(w * 0.08f, h * 0.16f)
                    )
                    drawOval(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.80f, h * 0.35f),
                        size = Size(w * 0.08f, h * 0.16f)
                    )

                    // Body
                    drawRoundRect(
                        brush = bodyBrush,
                        topLeft = Offset(w * 0.26f, h * 0.72f),
                        size = Size(w * 0.48f, h * 0.26f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, h * 0.12f)
                    )

                    // Center Heart Light
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = w * 0.045f,
                        center = Offset(w * 0.5f, h * 0.82f)
                    )
                }
            }
    )
}

// ─── Section Header with "See All" ───
@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "See All",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = PrimaryPurple,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSeeAll)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

// ─── Single Row of 3 Tools for Lazy Recycling ───
@Composable
private fun ToolGridRow(
    rowTools: List<RedesignToolItem>,
    onToolClick: (Tool) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rowTools.forEach { toolItem ->
            Box(modifier = Modifier.weight(1f)) {
                RedesignToolCard(
                    tool = toolItem,
                    onClick = {
                        onToolClick(Tool(toolItem.title.replace("\n", " "), "", toolItem.icon, toolItem.gradientColors.first()))
                    }
                )
            }
        }
        if (rowTools.size < 3) {
            repeat(3 - rowTools.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ─── Single Tool Card (Optimized for 60/120 FPS Scrolling) ───
@Composable
private fun RedesignToolCard(
    tool: RedesignToolItem,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) DarkSurface else LightSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) DarkBorder else LightBorder
        ),
        shadowElevation = if (isDark) 0.dp else 1.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Container (46dp x 46dp Rounded Square with Precomputed Gradient Brush)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush = tool.brush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title
            Text(
                text = tool.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
