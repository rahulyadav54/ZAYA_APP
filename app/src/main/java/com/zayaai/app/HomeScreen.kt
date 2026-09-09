package com.zayaai.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(userName: String, innerPadding: PaddingValues, onToolClick: (Tool) -> Unit) {
    // Add a Zaya Edu tool for educational resources
    val tools = listOf(
        Tool("Student Tools", "Academic success toolkit", Icons.Rounded.School, Color(0xFF6E72FC)),
        Tool("Brain Games", "Offline puzzles & challenges", Icons.Rounded.SportsEsports, Color(0xFFFF6D00)),
        Tool("AI Chat", "Advanced conversational AI", Icons.AutoMirrored.Rounded.Chat, Color(0xFF9D50BB)),
        Tool("Zaya Edu", "Explore educational resources", Icons.Rounded.Public, Color(0xFF42A5F5)),
        Tool("Image Gen", "Create art from text", Icons.Rounded.Image, Color(0xFF6E72FC)),
        Tool("Code Gen", "Generate code snippets", Icons.Rounded.Code, Color(0xFF00E676)),
        Tool("Text Writer", "AI-powered copywriting", Icons.Rounded.Edit, Color(0xFFFFD600)),
        Tool("Quiz Gen", "Create interactive quizzes", Icons.Rounded.Quiz, Color(0xFFFF5252)),
        Tool("PPT Gen", "AI PowerPoint presentations", Icons.Rounded.Slideshow, Color(0xFF26C6DA)),
        Tool("Prompt Engineer", "Generate & optimize prompts", Icons.Rounded.AutoFixHigh, Color(0xFFAB47BC)),
        Tool("AI Study", "Upload & learn with AI", Icons.Rounded.School, Color(0xFFFF9800)),
        Tool("Prompt Library", "Ready-to-use AI prompts", Icons.Rounded.LibraryBooks, Color(0xFFE040FB)),
        Tool("Startup Idea", "AI startup idea generator", Icons.Rounded.Lightbulb, Color(0xFFFFAB00)),
        Tool("Resume Builder", "ATS resumes & job matcher", Icons.Rounded.Description, Color(0xFF26A69A))
    )

    // Load admin-managed content from Firestore for students
    val studentContentViewModel: StudentContentViewModel = viewModel()
    
    // Safe content display state
    var showAnnouncements by remember { mutableStateOf(true) }
    var showArticles by remember { mutableStateOf(true) }
    var showQuizzes by remember { mutableStateOf(true) }
    var showFormulas by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Welcome back, $userName", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Explore AI Tools",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Tools Grid (fixed height so it doesn't nest scrolling)
        item {
            // Use a non-lazy grid to avoid nested scrolling issues
            val rows = tools.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                rows.forEach { rowTools ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowTools.forEach { tool ->
                            Box(modifier = Modifier.weight(1f)) {
                                ToolCard(tool, onClick = { onToolClick(tool) })
                            }
                        }
                        // If odd number, add empty spacer
                        if (rowTools.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Admin-managed Announcements
        if (studentContentViewModel.announcements.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StudentAnnouncementsSection(studentContentViewModel.announcements)
            }
        }

        // Admin-managed Quizzes
        if (studentContentViewModel.quizzes.isNotEmpty()) {
            item {
                var selectedQuiz by remember { mutableStateOf<FirestoreQuiz?>(null) }
                StudentQuizzesSection(
                    quizzes = studentContentViewModel.quizzes,
                    onQuizClick = { selectedQuiz = it }
                )
                if (selectedQuiz != null) {
                    StudentQuizPlayerDialog(
                        quiz = selectedQuiz!!,
                        onDismiss = { selectedQuiz = null }
                    )
                }
            }
        }

        // Admin-managed Formulas
        if (studentContentViewModel.formulas.isNotEmpty()) {
            item {
                StudentFormulasSection(studentContentViewModel.formulas)
            }
        }

        // Admin-managed Articles
        if (studentContentViewModel.articles.isNotEmpty()) {
            item {
                StudentArticlesSection(studentContentViewModel.articles)
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ErrorMessageCard(title: String, message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Retry")
            }
        }
    }
}
