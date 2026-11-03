package com.zayaai.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ads.BannerAdCard
import com.zayaai.app.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MathProblem(
    val question: String,
    val answer: Int,
    val options: List<Int>
)

@Composable
fun MathChallengeScreen(paddingValues: PaddingValues) {
    val isDark = LocalIsDarkTheme.current

    var gameState by remember { mutableStateOf("menu") } // menu, playing, result
    var difficulty by remember { mutableStateOf("Easy") }
    var score by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableIntStateOf(0) }
    var totalQuestions by remember { mutableIntStateOf(10) }
    var timeLeft by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }
    var problem by remember { mutableStateOf<MathProblem?>(null) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var totalTimeLimit by remember { mutableIntStateOf(60) }

    // Generate math problem
    fun generateProblem(): MathProblem {
        val (a, b, op, answer) = when (difficulty) {
            "Easy" -> {
                val a = Random.nextInt(1, 20)
                val b = Random.nextInt(1, 20)
                val ops = listOf("+", "-")
                val op = ops.random()
                val ans = if (op == "+") a + b else a - b
                listOf(a, b, op, ans)
            }
            "Medium" -> {
                val ops = listOf("+", "-", "×")
                val op = ops.random()
                val a: Int
                val b: Int
                val ans: Int
                when (op) {
                    "×" -> { a = Random.nextInt(2, 13); b = Random.nextInt(2, 13); ans = a * b }
                    "-" -> { a = Random.nextInt(10, 100); b = Random.nextInt(1, a); ans = a - b }
                    else -> { a = Random.nextInt(10, 100); b = Random.nextInt(10, 100); ans = a + b }
                }
                listOf(a, b, op, ans)
            }
            else -> { // Hard
                val ops = listOf("+", "-", "×", "÷")
                val op = ops.random()
                val a: Int
                val b: Int
                val ans: Int
                when (op) {
                    "×" -> { a = Random.nextInt(5, 25); b = Random.nextInt(5, 25); ans = a * b }
                    "÷" -> { b = Random.nextInt(2, 15); ans = Random.nextInt(2, 15); a = b * ans }
                    "-" -> { a = Random.nextInt(50, 200); b = Random.nextInt(10, a); ans = a - b }
                    else -> { a = Random.nextInt(50, 200); b = Random.nextInt(50, 200); ans = a + b }
                }
                listOf(a, b, op, ans)
            }
        }

        val correctAnswer = answer as Int
        val options = mutableSetOf(correctAnswer)
        while (options.size < 4) {
            val offset = Random.nextInt(-10, 11)
            if (offset != 0) options.add(correctAnswer + offset)
        }

        return MathProblem(
            question = "$a $op $b = ?",
            answer = correctAnswer,
            options = options.toList().shuffled()
        )
    }

    // Timer
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            timeLeft = totalTimeLimit
            while (timeLeft > 0 && gameState == "playing") {
                delay(1000L)
                timeLeft--
            }
            if (gameState == "playing") {
                gameState = "result"
            }
        }
    }

    // Auto-advance after answer
    LaunchedEffect(selectedAnswer) {
        if (selectedAnswer != null) {
            delay(800L)
            if (currentQuestion + 1 >= totalQuestions) {
                gameState = "result"
            } else {
                currentQuestion++
                problem = generateProblem()
                selectedAnswer = null
                isCorrect = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (gameState) {
            "menu" -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text("🧮", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Math Challenge",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Solve math problems against the clock!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Difficulty selector
                Text(
                    "Select Difficulty",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    Triple("Easy", Color(0xFF00E676), "Add & Subtract (1-20)"),
                    Triple("Medium", Color(0xFFFFAB00), "Include Multiply (up to 100)"),
                    Triple("Hard", Color(0xFFFF5252), "All operations (big numbers)")
                ).forEach { (diff, color, desc) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (difficulty == diff)
                                color.copy(alpha = if (isDark) 0.2f else 0.12f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (difficulty == diff) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
                        onClick = { difficulty = diff }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                colors = RadioButtonDefaults.colors(selectedColor = color)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(diff, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        score = 0
                        currentQuestion = 0
                        streak = 0
                        bestStreak = 0
                        selectedAnswer = null
                        isCorrect = null
                        totalTimeLimit = when (difficulty) {
                            "Easy" -> 90
                            "Medium" -> 75
                            else -> 60
                        }
                        problem = generateProblem()
                        gameState = "playing"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Challenge", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            "playing" -> {
                problem?.let { p ->
                    // Stats bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Timer
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (timeLeft <= 10)
                                    Color(0xFFFF5252).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Timer, null, tint = if (timeLeft <= 10) Color(0xFFFF5252) else NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${timeLeft}s", fontWeight = FontWeight.Bold, color = if (timeLeft <= 10) Color(0xFFFF5252) else MaterialTheme.colorScheme.onBackground)
                            }
                        }

                        // Score
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$score", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }

                        // Streak
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$streak", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }

                    // Progress
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { (currentQuestion + 1).toFloat() / totalQuestions },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        "Question ${currentQuestion + 1} of $totalQuestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Question card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                p.question,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Options grid (2x2)
                    val rows = p.options.chunked(2)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { option ->
                                val bgColor = when {
                                    selectedAnswer == null -> MaterialTheme.colorScheme.surface
                                    option == p.answer -> Color(0xFF00E676).copy(alpha = 0.2f)
                                    option == selectedAnswer -> Color(0xFFFF5252).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val borderColor = when {
                                    selectedAnswer == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    option == p.answer -> Color(0xFF00E676)
                                    option == selectedAnswer -> Color(0xFFFF5252)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgColor),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
                                    onClick = {
                                        if (selectedAnswer == null) {
                                            selectedAnswer = option
                                            if (option == p.answer) {
                                                isCorrect = true
                                                score += when (difficulty) {
                                                    "Easy" -> 10
                                                    "Medium" -> 20
                                                    else -> 30
                                                }
                                                streak++
                                                if (streak > bestStreak) bestStreak = streak
                                            } else {
                                                isCorrect = false
                                                streak = 0
                                            }
                                        }
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$option",
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            "result" -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text(if (score > totalQuestions * 15) "🏆" else "⭐", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (score > totalQuestions * 15) "Excellent!" else if (score > totalQuestions * 8) "Good Job!" else "Keep Practicing!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ResultStatRow("📊", "Score", "$score points")
                        ResultStatRow("🎯", "Difficulty", difficulty)
                        ResultStatRow("🔥", "Best Streak", "$bestStreak")
                        ResultStatRow("❓", "Questions", "$totalQuestions")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { gameState = "menu" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Icon(Icons.Rounded.Replay, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                BannerAdCard()
            }
        }
    }
}

@Composable
fun ResultStatRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}
