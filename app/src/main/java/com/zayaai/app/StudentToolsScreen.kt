package com.zayaai.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

data class StudentToolItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

@Composable
fun StudentToolsScreen(
    paddingValues: PaddingValues,
    onToolClick: (Screen) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    val studentTools = remember {
        listOf(
            StudentToolItem(Screen.Calculator, "Scientific\nCalculator", Screen.Calculator.icon, listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
            StudentToolItem(Screen.UnitConverter, "Unit\nConverter", Screen.UnitConverter.icon, listOf(Color(0xFF10B981), Color(0xFF059669))),
            StudentToolItem(Screen.WordCounter, "Word\nCounter", Screen.WordCounter.icon, listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
            StudentToolItem(Screen.FormulaLibrary, "Formula\nLibrary", Screen.FormulaLibrary.icon, listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
            StudentToolItem(Screen.Notes, "Study\nNotes", Screen.Notes.icon, listOf(Color(0xFFEC4899), Color(0xFFDB2777))),
            StudentToolItem(Screen.Flashcards, "Revision\nFlashcards", Screen.Flashcards.icon, listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
            StudentToolItem(Screen.OfflineQuiz, "Offline\nQuiz", Screen.OfflineQuiz.icon, listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
            StudentToolItem(Screen.MindMap, "Mind\nMap", Screen.MindMap.icon, listOf(Color(0xFF14B8A6), Color(0xFF0D9488))),
            StudentToolItem(Screen.StudyPlanner, "Study\nPlanner", Screen.StudyPlanner.icon, listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))),
            StudentToolItem(Screen.Pomodoro, "Pomodoro\nTimer", Screen.Pomodoro.icon, listOf(Color(0xFFF43F5E), Color(0xFFE11D48))),
            StudentToolItem(Screen.ToDo, "Tasks &\nTo-Do", Screen.ToDo.icon, listOf(Color(0xFFA855F7), Color(0xFF9333EA))),
            StudentToolItem(Screen.TextFormatter, "Text\nFormatter", Screen.TextFormatter.icon, listOf(Color(0xFF06B6D4), Color(0xFF0891B2))),
            StudentToolItem(Screen.Dictionary, "Word\nDictionary", Screen.Dictionary.icon, listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
            StudentToolItem(Screen.BookmarkManager, "Bookmarks\nManager", Screen.BookmarkManager.icon, listOf(Color(0xFFD946EF), Color(0xFFC026D3))),
            StudentToolItem(Screen.StudyArticles, "Study\nArticles", Screen.StudyArticles.icon, listOf(Color(0xFF22C55E), Color(0xFF16A34A))),
            StudentToolItem(Screen.OfflineAssistant, "Offline\nAssistant", Screen.OfflineAssistant.icon, listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Student Tools",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = if (isDark) DarkTextPrimary else LightTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Academic tools to study, calculate, organize and revise.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = if (isDark) DarkTextSecondary else LightTextSecondary,
            modifier = Modifier.padding(bottom = 18.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(studentTools) { item ->
                StudentGridCard(
                    tool = item,
                    onClick = { onToolClick(item.screen) }
                )
            }
        }
    }
}

@Composable
private fun StudentGridCard(
    tool: StudentToolItem,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "student_tool_scale"
    )

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
            .height(112.dp)
            .scale(scale)
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
                    .background(
                        brush = Brush.linearGradient(tool.gradientColors),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .shadow(
                        elevation = if (isDark) 4.dp else 4.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = tool.gradientColors.first().copy(alpha = 0.35f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title below icon
            Text(
                text = tool.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                ),
                color = if (isDark) DarkTextPrimary else LightTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
