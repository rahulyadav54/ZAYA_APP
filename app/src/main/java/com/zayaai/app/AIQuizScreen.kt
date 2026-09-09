package com.zayaai.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun AIQuizScreen(
    viewModel: StudyMaterialViewModel,
    paddingValues: PaddingValues
) {
    val questions = viewModel.quizQuestions

    LaunchedEffect(Unit) { viewModel.startQuiz() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Quiz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No quiz questions generated yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return
        }

        if (viewModel.quizCompleted) {
            // Results Screen
            QuizResultsView(viewModel)
            return
        }

        val current = questions[viewModel.currentQuizIndex]

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Question ${viewModel.currentQuizIndex + 1}/${questions.size}",
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (current.difficulty) {
                        "easy" -> Color(0xFF00E676).copy(alpha = 0.2f)
                        "hard" -> Color(0xFFFF5252).copy(alpha = 0.2f)
                        else -> Color(0xFFFFD600).copy(alpha = 0.2f)
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    current.difficulty.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = when (current.difficulty) {
                        "easy" -> Color(0xFF00E676)
                        "hard" -> Color(0xFFFF5252)
                        else -> Color(0xFFFFD600)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Progress bar
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { (viewModel.currentQuizIndex + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = NeonCyan,
            trackColor = MaterialTheme.colorScheme.surface,
            drawStopIndicator = {}
        )

        // Question type badge
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            when (current.questionType) {
                "true_false" -> "TRUE / FALSE"
                "short_answer" -> "SHORT ANSWER"
                else -> "MULTIPLE CHOICE"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        // Question
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            current.question,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        if (current.questionType == "short_answer") {
            var shortAnswer by remember(viewModel.currentQuizIndex) { mutableStateOf("") }

            OutlinedTextField(
                value = shortAnswer,
                onValueChange = {
                    shortAnswer = it
                    viewModel.selectAnswer(it)
                },
                placeholder = { Text("Type your answer...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.showAnswer
            )
        } else {
            val options = listOfNotNull(
                current.optionA.ifBlank { null },
                current.optionB.ifBlank { null },
                current.optionC.ifBlank { null },
                current.optionD.ifBlank { null }
            )
            options.forEach { option ->
                val isSelected = viewModel.selectedAnswer == option
                val isCorrect = option.equals(current.correctAnswer, ignoreCase = true)
                val bgColor by animateColorAsState(
                    when {
                        viewModel.showAnswer && isCorrect -> Color(0xFF00E676).copy(alpha = 0.2f)
                        viewModel.showAnswer && isSelected && !isCorrect -> Color(0xFFFF5252).copy(alpha = 0.2f)
                        isSelected -> NeonCyan.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    label = "optionBg"
                )
                val borderColor by animateColorAsState(
                    when {
                        viewModel.showAnswer && isCorrect -> Color(0xFF00E676)
                        viewModel.showAnswer && isSelected && !isCorrect -> Color(0xFFFF5252)
                        isSelected -> NeonCyan
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    label = "optionBorder"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !viewModel.showAnswer) { viewModel.selectAnswer(option) },
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                        if (viewModel.showAnswer && isCorrect) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF00E676), modifier = Modifier.size(22.dp))
                        }
                        if (viewModel.showAnswer && isSelected && !isCorrect) {
                            Icon(Icons.Rounded.Cancel, null, tint = Color(0xFFFF5252), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit / Next buttons
        if (!viewModel.showAnswer) {
            Button(
                onClick = { viewModel.submitAnswer() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(14.dp),
                enabled = viewModel.selectedAnswer != null
            ) {
                Text("Submit Answer", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (viewModel.currentQuizIndex < questions.size - 1) "Next Question" else "See Results",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun QuizResultsView(viewModel: StudyMaterialViewModel) {
    val total = viewModel.quizQuestions.size
    val score = viewModel.quizScore
    val percent = if (total > 0) (score * 100) / total else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Icon(
            if (percent >= 80) Icons.Rounded.EmojiEvents
            else if (percent >= 50) Icons.Rounded.ThumbUp
            else Icons.Rounded.Refresh,
            null,
            tint = if (percent >= 80) Color(0xFFFFD600) else if (percent >= 50) NeonCyan else Color(0xFFFF5252),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            when {
                percent >= 80 -> "Excellent!"
                percent >= 50 -> "Good Job!"
                else -> "Keep Practicing!"
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "$score / $total correct ($percent%)",
            color = NeonCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Adaptive Difficulty", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Next quiz difficulty: ${viewModel.currentDifficulty.replaceFirstChar { it.uppercase() }}",
                    color = when (viewModel.currentDifficulty) {
                        "easy" -> Color(0xFF00E676)
                        "hard" -> Color(0xFFFF5252)
                        else -> Color(0xFFFFD600)
                    },
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when {
                        percent > 80 -> "Score > 80% → Difficulty increased"
                        percent < 50 -> "Score < 50% → Difficulty decreased"
                        else -> "Score 50-80% → Difficulty unchanged"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.startQuiz() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Rounded.Refresh, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retake Quiz", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


