package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun MaterialDetailScreen(
    viewModel: StudyMaterialViewModel,
    paddingValues: PaddingValues,
    onNavigate: (Screen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                viewModel.currentMaterialTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "AI-generated learning content",
                color = NeonCyan,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Dashboard cards
        item {
            DashboardCard(
                icon = Icons.Rounded.HelpOutline,
                title = "Important Questions",
                subtitle = "${viewModel.generatedQuestions.size} questions generated",
                color = Color(0xFFFF9800),
                onClick = { onNavigate(Screen.GeneratedQuestions) }
            )
        }
        item {
            DashboardCard(
                icon = Icons.Rounded.Quiz,
                title = "Take AI Quiz",
                subtitle = "${viewModel.quizQuestions.size} questions  •  Difficulty: ${viewModel.currentDifficulty.replaceFirstChar { it.uppercase() }}",
                color = Color(0xFFFF5252),
                onClick = { onNavigate(Screen.AIQuiz) }
            )
        }
        item {
            DashboardCard(
                icon = Icons.Rounded.Style,
                title = "Flashcards",
                subtitle = "${viewModel.flashcards.size} cards to review",
                color = NeonCyan,
                onClick = { onNavigate(Screen.AIFlashcards) }
            )
        }
        item {
            DashboardCard(
                icon = Icons.Rounded.Summarize,
                title = "Study Summary",
                subtitle = if (viewModel.studySummary != null) "Summary ready" else "Not available",
                color = Color(0xFF00E676),
                onClick = { onNavigate(Screen.StudySummary) }
            )
        }
        item {
            DashboardCard(
                icon = Icons.Rounded.School,
                title = "AI Tutor Chat",
                subtitle = "Ask questions about this material",
                color = NeonPurple,
                onClick = { onNavigate(Screen.AITutor) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun DashboardCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


