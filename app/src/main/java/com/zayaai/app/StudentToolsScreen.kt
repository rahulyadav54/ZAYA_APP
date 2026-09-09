package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun StudentToolsScreen(paddingValues: PaddingValues, onToolClick: (Screen) -> Unit) {
    val studentTools = listOf(
        ToolData(Screen.OfflineAssistant, "AI study helper — works without internet!", Color(0xFF00E676)),
        ToolData(Screen.Notes, "Create and manage study notes.", NeonPurple),
        ToolData(Screen.Flashcards, "Create flashcards for quick revision.", NeonCyan),
        ToolData(Screen.OfflineQuiz, "Practice quiz questions without internet.", Color(0xFFFF5252)),
        ToolData(Screen.FormulaLibrary, "Browse important math and science formulas.", Color(0xFF00E676)),
        ToolData(Screen.MindMap, "Create visual diagrams for ideas.", Color(0xFFFFD600)),
        ToolData(Screen.StudyPlanner, "Plan daily study schedules.", Color(0xFF6E72FC)),
        ToolData(Screen.Pomodoro, "Focus timer for study sessions.", Color(0xFFFF5252)),
        ToolData(Screen.ToDo, "Manage study tasks.", Color(0xFFAD1DEB)),
        ToolData(Screen.Calculator, "Advanced calculator for math.", NeonCyan),
        ToolData(Screen.UnitConverter, "Convert units quickly.", NeonPurple),
        ToolData(Screen.WordCounter, "Count words and characters in text.", Color(0xFF00E676)),
        ToolData(Screen.TextFormatter, "Convert text to uppercase, lowercase, and more.", Color(0xFFFFD600)),
        ToolData(Screen.Dictionary, "Search word meanings offline.", Color(0xFF6E72FC)),
        ToolData(Screen.BookmarkManager, "Save important notes and resources.", Color(0xFFAD1DEB)),
        ToolData(Screen.StudyArticles, "Read offline educational content.", NeonCyan)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Student Tools",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Everything you need for your academic success.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(studentTools) { tool ->
                StudentToolCard(tool) { onToolClick(tool.screen) }
            }
        }
    }
}

data class ToolData(
    val screen: Screen,
    val description: String,
    val color: Color
)

@Composable
fun StudentToolCard(tool: ToolData, onClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current
    Card(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 4.dp
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    tool.color.copy(alpha = if (isDark) 0.6f else 0.4f),
                    if (isDark) Color.Transparent else tool.color.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle gradient overlay at top for light mode
            if (!isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    tool.color.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            tool.color.copy(alpha = if (isDark) 0.15f else 0.12f),
                            CircleShape
                        )
                        .border(1.dp, tool.color.copy(alpha = if (isDark) 0.3f else 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.screen.icon,
                        contentDescription = tool.screen.title,
                        tint = tool.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = tool.screen.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

