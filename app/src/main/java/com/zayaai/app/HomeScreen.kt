package com.zayaai.app

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
data class RedesignToolItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val category: String,
    val gradientColors: List<Color>,
    val route: String
)

@Composable
fun HomeScreen(
    userName: String,
    innerPadding: PaddingValues,
    onToolClick: (Tool) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    // Clean display name (defaults to "Rahul" if none provided to match mockup)
    val displayName = remember(userName) {
        if (userName.isBlank() || userName.equals("User", ignoreCase = true)) "Rahul" else userName
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
            RedesignToolItem("brain_games", "Brain Games", Icons.Rounded.SportsEsports, "Creative", listOf(Color(0xFFEC4899), Color(0xFF9333EA)), Screen.BrainGames.route)
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
                text = "Hello, $displayName 👋",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Explore powerful AI tools to study, create, code, write and be more productive.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ─── 2. AI Chat Hero Card ───
        item(key = "home_hero_card") {
            AIChatHeroCard(
                onCardClick = {
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
                key = { row -> "ai_row_" + row.joinToString("-") { it.id } }
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
                key = { row -> "student_row_" + row.joinToString("-") { it.id } }
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
                key = { row -> "prod_row_" + row.joinToString("-") { it.id } }
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
                key = { row -> "creative_row_" + row.joinToString("-") { it.id } }
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

// ─── AI Chat Hero Card (Featured Banner with 3D Robot Assistant) ───
@Composable
private fun AIChatHeroCard(onCardClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(22.dp)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onCardClick)
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
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Left Content: Title, Subtitle, Action Button
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.65f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI Chat",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chat with AI ZAYA\nfor anything",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Circle Arrow Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (isDark) Color(0xFF7C3AED) else Color.White,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open AI Chat",
                        tint = if (isDark) Color.White else PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Right Graphic: Sleek Futuristic 3D Assistant Robot Character with Cached Drawing
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(105.dp),
                contentAlignment = Alignment.Center
            ) {
                FuturisticRobotGraphic()
            }
        }
    }
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
        shadowElevation = if (isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Container (46dp x 46dp Rounded Square with Gradient)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush = Brush.linearGradient(tool.gradientColors)),
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
