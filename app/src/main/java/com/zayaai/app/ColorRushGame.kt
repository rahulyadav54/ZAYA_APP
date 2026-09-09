package com.zayaai.app

import androidx.compose.foundation.background
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
import kotlinx.coroutines.delay
import kotlin.random.Random

private val gameColors = listOf(
    Pair("Red", Color(0xFFFF5252)),
    Pair("Blue", Color(0xFF448AFF)),
    Pair("Green", Color(0xFF00E676)),
    Pair("Yellow", Color(0xFFFFD600)),
    Pair("Purple", Color(0xFFE040FB)),
    Pair("Orange", Color(0xFFFF9800)),
    Pair("Cyan", Color(0xFF00BCD4)),
    Pair("Pink", Color(0xFFFF4081))
)

@Composable
fun ColorRushScreen(paddingValues: PaddingValues) {
    val isDark = LocalIsDarkTheme.current

    var gameState by remember { mutableStateOf("menu") }
    var score by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var totalRounds by remember { mutableIntStateOf(20) }
    var lives by remember { mutableIntStateOf(3) }
    var displayColorName by remember { mutableStateOf("") }
    var displayColor by remember { mutableStateOf(Color.White) }
    var correctAnswer by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf<Pair<String, Color>>()) }
    var streak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf("color") } // "color" = tap the COLOR shown, "name" = tap the NAME shown
    var timeLeft by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    fun generateRound() {
        selectedOption = null
        val isTricky = Random.nextFloat() > 0.4f // 60% of rounds are tricky (mismatched)

        val targetColor = gameColors.random()
        val distractorColor = gameColors.filter { it.first != targetColor.first }.random()

        if (isTricky) {
            // Show a color NAME written in a DIFFERENT color
            displayColorName = targetColor.first
            displayColor = distractorColor.second
        } else {
            // Normal: name matches color
            displayColorName = targetColor.first
            displayColor = targetColor.second
        }

        if (mode == "color") {
            // User must tap the button matching the INK COLOR (not the word)
            correctAnswer = gameColors.first { it.second == displayColor }.first
        } else {
            // User must tap the button matching the WORD
            correctAnswer = displayColorName
        }

        // Generate options
        val allOptions = mutableSetOf(correctAnswer)
        while (allOptions.size < 4) {
            allOptions.add(gameColors.random().first)
        }
        options = allOptions.map { name ->
            Pair(name, gameColors.first { it.first == name }.second)
        }.shuffled()
    }

    // Timer
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            timeLeft = 45
            while (timeLeft > 0 && gameState == "playing") {
                delay(1000L)
                timeLeft--
            }
            if (gameState == "playing") {
                gameState = "result"
            }
        }
    }

    // Auto-advance after selection
    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            delay(500L)
            if (round + 1 >= totalRounds) {
                gameState = "result"
            } else {
                round++
                generateRound()
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
                Text("🎨", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Color Rush",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "The Stroop Effect challenge!\nYour brain vs. your eyes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Mode selector
                Text("Game Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple("color", "Match the INK Color", "Tap the button matching the color of the text"),
                    Triple("name", "Match the Word", "Tap the button matching what the word says")
                ).forEach { (modeVal, title, desc) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mode == modeVal)
                                Color(0xFFFF5252).copy(alpha = if (isDark) 0.2f else 0.12f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (mode == modeVal) 
                            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252)) else null,
                        onClick = { mode = modeVal }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = mode == modeVal,
                                onClick = { mode = modeVal },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5252))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Example
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Example", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Blue",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF5252) // Written in RED
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (mode == "color") "👆 You'd tap: Red (the ink color)"
                            else "👆 You'd tap: Blue (the word)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        score = 0
                        round = 0
                        lives = 3
                        streak = 0
                        bestStreak = 0
                        generateRound()
                        gameState = "playing"
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Rush!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            "playing" -> {
                // Stats bar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                        containerColor = if (timeLeft <= 10) Color(0xFFFF5252).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    )) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Timer, null, tint = if (timeLeft <= 10) Color(0xFFFF5252) else NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${timeLeft}s", fontWeight = FontWeight.Bold, color = if (timeLeft <= 10) Color(0xFFFF5252) else MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$score", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            repeat(lives) { Text("❤️", fontSize = 14.sp) }
                            repeat(3 - lives) { Text("🖤", fontSize = 14.sp) }
                        }
                    }
                }

                // Progress
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (round + 1).toFloat() / totalRounds },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF5252),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Instruction
                Text(
                    if (mode == "color") "Tap the INK COLOR 🎨" else "Tap the WORD 📝",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Display word in color
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayColorName,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = displayColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (streak >= 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔥 $streak streak!", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Answer options (2x2)
                val optionRows = options.chunked(2)
                optionRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { (name, color) ->
                            val isSelected = selectedOption == name
                            val isCorrectChoice = name == correctAnswer
                            val bgColor = when {
                                selectedOption == null -> color.copy(alpha = 0.15f)
                                isSelected && isCorrectChoice -> Color(0xFF00E676).copy(alpha = 0.3f)
                                isSelected && !isCorrectChoice -> Color(0xFFFF5252).copy(alpha = 0.3f)
                                !isSelected && isCorrectChoice -> Color(0xFF00E676).copy(alpha = 0.2f)
                                else -> color.copy(alpha = 0.08f)
                            }

                            Card(
                                modifier = Modifier.weight(1f).height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.4f)),
                                onClick = {
                                    if (selectedOption == null) {
                                        selectedOption = name
                                        if (name == correctAnswer) {
                                            score += 15 + streak * 5
                                            streak++
                                            if (streak > bestStreak) bestStreak = streak
                                        } else {
                                            streak = 0
                                            lives--
                                            if (lives <= 0) {
                                                gameState = "result"
                                            }
                                        }
                                    }
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            "result" -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text(if (score >= 200) "🏆" else "🎨", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (score >= 200) "Color Master!" else if (score >= 100) "Great Focus!" else "Keep Practicing!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ResultStatRow("📊", "Score", "$score points")
                        ResultStatRow("🔥", "Best Streak", "$bestStreak")
                        ResultStatRow("🎮", "Mode", if (mode == "color") "Match INK Color" else "Match Word")
                        ResultStatRow("🏁", "Rounds", "${round + 1}/$totalRounds")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { gameState = "menu" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Icon(Icons.Rounded.Replay, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
