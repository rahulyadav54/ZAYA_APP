package com.zayaai.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun QuizScreen(viewModel: QuizViewModel, paddingValues: PaddingValues) {
    val quizQuestions by viewModel.quizQuestions.collectAsState()
    val currentQuestionIndex = viewModel.currentQuestionIndex
    val selectedOption = viewModel.selectedOption
    val quizCompleted = viewModel.quizCompleted
    val isSeeding = viewModel.isSeeding
    val currentQuestion = quizQuestions.getOrNull(currentQuestionIndex)
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Quiz, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Offline Quiz",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            // Add question button
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                        CircleShape
                    )
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Question", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        if (quizQuestions.isEmpty() || isSeeding) {
            // Loading state while seeding
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading quiz questions...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (quizCompleted) {
            QuizResultScreen(viewModel = viewModel, totalQuestions = quizQuestions.size) {
                viewModel.resetQuiz()
            }
        } else if (currentQuestion != null) {
            // Progress bar
            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1).toFloat() / quizQuestions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonCyan,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Question counter + category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${quizQuestions.size}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .background(NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentQuestion.category.ifEmpty { "General" },
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(targetState = currentQuestionIndex, label = "question_transition") { index ->
                val question = quizQuestions[index]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    QuizQuestionCard(question)
                    Spacer(modifier = Modifier.height(24.dp))
                    QuizOptions(question, selectedOption) { option ->
                        viewModel.selectOption(option)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    GradientButton(
                        text = if (currentQuestionIndex < quizQuestions.size - 1) "Next" else "Finish",
                        onClick = { viewModel.moveToNextQuestion() },
                        isLoading = false,
                        enabled = selectedOption != null
                    )
                }
            }
        }
    }

    // Add Question Dialog
    if (showAddDialog) {
        AddQuizQuestionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { question, a, b, c, d, correct, category ->
                viewModel.addQuestion(question, a, b, c, d, correct, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun QuizQuestionCard(question: QuizQuestionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuizOptions(question: QuizQuestionEntity, selectedOption: String?, onOptionSelected: (String) -> Unit) {
    val labels = listOf("A", "B", "C", "D")
    val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onOptionSelected(option) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option letter badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isSelected) NeonCyan else MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        labels[index],
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun QuizResultScreen(viewModel: QuizViewModel, totalQuestions: Int, onRetakeQuiz: () -> Unit) {
    val score = viewModel.score
    val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions * 100).toInt() else 0
    val resultColor = when {
        percentage >= 80 -> Color(0xFF00E676)
        percentage >= 50 -> Color(0xFFFFD600)
        else -> Color(0xFFFF5252)
    }
    val resultEmoji = when {
        percentage >= 80 -> "ðŸŽ‰"
        percentage >= 50 -> "ðŸ‘"
        else -> "ðŸ“š"
    }
    val resultMessage = when {
        percentage >= 80 -> "Excellent work!"
        percentage >= 50 -> "Good effort!"
        else -> "Keep practicing!"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            resultEmoji,
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "Quiz Completed!",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            resultMessage,
            style = MaterialTheme.typography.titleMedium,
            color = resultColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Score circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(resultColor.copy(alpha = 0.1f), CircleShape)
                .border(3.dp, resultColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = resultColor
                )
                Text(
                    "$score/$totalQuestions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        GradientButton("Retake Quiz", onClick = onRetakeQuiz)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuizQuestionDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General Knowledge") }
    val categories = listOf("General Knowledge", "Science", "Mathematics", "Technology", "History", "English")
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Quiz Question", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Category
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = NeonCyan, unfocusedBorderColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Question
                QuizTextField(value = question, onValueChange = { question = it }, label = "Question")
                Spacer(modifier = Modifier.height(8.dp))
                QuizTextField(value = optionA, onValueChange = { optionA = it }, label = "Option A")
                Spacer(modifier = Modifier.height(8.dp))
                QuizTextField(value = optionB, onValueChange = { optionB = it }, label = "Option B")
                Spacer(modifier = Modifier.height(8.dp))
                QuizTextField(value = optionC, onValueChange = { optionC = it }, label = "Option C")
                Spacer(modifier = Modifier.height(8.dp))
                QuizTextField(value = optionD, onValueChange = { optionD = it }, label = "Option D")
                Spacer(modifier = Modifier.height(12.dp))

                // Correct Answer picker
                Text("Correct Answer:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A" to optionA, "B" to optionB, "C" to optionC, "D" to optionD).forEach { (label, value) ->
                        val isSelected = correctAnswer == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { correctAnswer = value },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.3f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        question.isBlank() -> error = "Enter a question"
                        optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank() -> error = "Fill all options"
                        correctAnswer.isBlank() -> error = "Select the correct answer"
                        else -> onSave(question, optionA, optionB, optionC, optionD, correctAnswer, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) { Text("Add", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun QuizTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = NeonCyan, unfocusedBorderColor = MaterialTheme.colorScheme.surface
        )
    )
}

