package com.zayaai.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import com.zayaai.app.ui.theme.*

@Composable
fun StudentQuizPlayerDialog(
    quiz: FirestoreQuiz,
    onDismiss: () -> Unit
) {
    var currentQuestion by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var quizFinished by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (quizFinished) {
                    // Score screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val percentage = if (quiz.questions.isNotEmpty()) (score * 100) / quiz.questions.size else 0
                        val resultIcon = when {
                            percentage >= 80 -> Icons.Rounded.EmojiEvents
                            percentage >= 60 -> Icons.Rounded.ThumbUp
                            percentage >= 40 -> Icons.Rounded.MoreHoriz
                            else -> Icons.Rounded.TrendingDown
                        }

                        Icon(resultIcon, contentDescription = null, modifier = Modifier.size(64.dp), tint = NeonCyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Quiz Complete!",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "$score / ${quiz.questions.size}",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = NeonCyan
                        )
                        Text(
                            "$percentage% correct",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                currentQuestion = 0
                                selectedAnswer = -1
                                score = 0
                                answered = false
                                quizFinished = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry Quiz", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (quiz.questions.isNotEmpty() && currentQuestion < quiz.questions.size) {
                    val question = quiz.questions[currentQuestion]

                    // Progress
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (currentQuestion + 1).toFloat() / quiz.questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = NeonCyan,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    Text(
                        "Question ${currentQuestion + 1} of ${quiz.questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Question
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Options
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (question.options.isNotEmpty()) {
                            question.options.forEachIndexed { index, option ->
                                val isCorrect = index == question.correctAnswerIndex
                                val isSelected = index == selectedAnswer
                                val bgColor by animateColorAsState(
                                    targetValue = when {
                                        !answered -> if (isSelected) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        isCorrect -> Color(0xFF00E676).copy(alpha = 0.15f)
                                        isSelected && !isCorrect -> Color.Red.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    },
                                    label = "option_bg"
                                )
                                val borderColor by animateColorAsState(
                                    targetValue = when {
                                        !answered -> if (isSelected) NeonCyan else Color.Transparent
                                        isCorrect -> Color(0xFF00E676)
                                        isSelected && !isCorrect -> Color.Red
                                        else -> Color.Transparent
                                    },
                                    label = "option_border"
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !answered) {
                                            selectedAnswer = index
                                        },
                                    colors = CardDefaults.cardColors(containerColor = bgColor),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.5.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val label = ('A' + index).toString()
                                        Surface(
                                            color = if (isSelected && !answered) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text("No options available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action button
                    if (!answered) {
                        Button(
                            onClick = {
                                if (selectedAnswer >= 0) {
                                    answered = true
                                    if (selectedAnswer == question.correctAnswerIndex) {
                                        score++
                                    }
                                }
                            },
                            enabled = selectedAnswer >= 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                disabledContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Check Answer", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentQuestion < quiz.questions.size - 1) {
                                    currentQuestion++
                                    selectedAnswer = -1
                                    answered = false
                                } else {
                                    quizFinished = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(
                                if (currentQuestion < quiz.questions.size - 1) "Next Question" else "See Results",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("This quiz has no questions yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onDismiss) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}
