package com.zayaai.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.NeonCyan
import kotlinx.coroutines.delay

private val wordBank = listOf(
    "KOTLIN", "ANDROID", "COMPOSE", "PUZZLE", "BRAIN", "MEMORY",
    "QUICK", "SMART", "LOGIC", "THINK", "LEARN", "STUDY",
    "FOCUS", "TRAIN", "SHARP", "SOLVE", "GRADE", "BOOKS",
    "CLASS", "WRITE", "QUEST", "POWER", "SPEED", "SKILL",
    "GRAPH", "TABLE", "INPUT", "BUILD", "DEBUG", "STACK",
    "ARRAY", "QUEUE", "FLOAT", "PRINT", "VALUE", "SCORE",
    "TIMER", "LEVEL", "WORLD", "SPACE", "LIGHT", "SOUND",
    "MUSIC", "DANCE", "CLOUD", "STORM", "FLAME", "OCEAN",
    "RIVER", "MOUNT", "STONE", "EARTH", "PLANT", "BLOOM",
    "DREAM", "MAGIC", "ROYAL", "CROWN", "SWORD", "CHESS"
)

@Composable
fun WordScrambleScreen(paddingValues: PaddingValues) {
    var gameState by remember { mutableStateOf("menu") }
    var score by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var currentWord by remember { mutableStateOf("") }
    var scrambled by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var wordsCorrect by remember { mutableIntStateOf(0) }
    var totalWords by remember { mutableIntStateOf(10) }
    var timeLeft by remember { mutableIntStateOf(120) }
    var showError by remember { mutableStateOf(false) }
    var usedWords by remember { mutableStateOf(setOf<String>()) }

    fun scrambleWord(word: String): String {
        var result = word.toList().shuffled().joinToString("")
        var attempts = 0
        while (result == word && attempts < 10) {
            result = word.toList().shuffled().joinToString("")
            attempts++
        }
        return result
    }

    fun nextWord() {
        val available = wordBank.filter { it !in usedWords }
        if (available.isEmpty()) {
            usedWords = emptySet()
        }
        val pool = if (available.isEmpty()) wordBank else available
        currentWord = pool.random()
        usedWords = usedWords + currentWord
        scrambled = scrambleWord(currentWord)
        userInput = ""
        hint = ""
        showError = false
    }

    fun getHint() {
        if (hintsUsed < 3) {
            val revealCount = (hintsUsed + 1).coerceAtMost(currentWord.length / 2)
            hint = currentWord.take(revealCount) + "_".repeat(currentWord.length - revealCount)
            hintsUsed++
        }
    }

    // Timer
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            timeLeft = 120
            while (timeLeft > 0 && gameState == "playing") {
                delay(1000L)
                timeLeft--
            }
            if (gameState == "playing") {
                gameState = "result"
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
                Text("🔤", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Word Scramble",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Unscramble words against the clock!\nType the correct word to score points.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // How to play
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("How to Play", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(
                            "🔡 A scrambled word will appear",
                            "⌨️ Type the correct word",
                            "💡 Use hints if you're stuck (3 max)",
                            "⏱️ Beat the 2-minute timer!"
                        ).forEach { rule ->
                            Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        score = 0
                        wordsCorrect = 0
                        hintsUsed = 0
                        usedWords = emptySet()
                        nextWord()
                        gameState = "playing"
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Game", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            "playing" -> {
                // Stats bar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                        containerColor = if (timeLeft <= 15) Color(0xFFFF5252).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    )) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Timer, null, tint = if (timeLeft <= 15) Color(0xFFFF5252) else NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${timeLeft}s", fontWeight = FontWeight.Bold, color = if (timeLeft <= 15) Color(0xFFFF5252) else MaterialTheme.colorScheme.onBackground)
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
                            Text("✅", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$wordsCorrect", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Scrambled word
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Unscramble this word:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Letter tiles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            scrambled.forEach { letter ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$letter",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }

                        if (hint.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Hint: $hint", color = Color(0xFF448AFF), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Input
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it.uppercase().take(currentWord.length); showError = false },
                    placeholder = { Text("Type your answer...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = showError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                if (showError) {
                    Text("❌ Incorrect! Try again.", color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Hint button
                    OutlinedButton(
                        onClick = { getHint() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = hintsUsed < 3
                    ) {
                        Text("💡 Hint (${3 - hintsUsed})", fontWeight = FontWeight.Bold)
                    }

                    // Skip
                    OutlinedButton(
                        onClick = { nextWord() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("⏭️ Skip", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submit
                Button(
                    onClick = {
                        if (userInput.equals(currentWord, ignoreCase = true)) {
                            wordsCorrect++
                            score += 25 + (timeLeft / 10) // bonus for speed
                            nextWord()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    enabled = userInput.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.Check, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            "result" -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text(if (wordsCorrect >= 8) "🏆" else "🔤", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (wordsCorrect >= 8) "Word Master!" else if (wordsCorrect >= 5) "Good Job!" else "Keep Practicing!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ResultStatRow("📊", "Score", "$score points")
                        ResultStatRow("✅", "Words Solved", "$wordsCorrect")
                        ResultStatRow("💡", "Hints Used", "$hintsUsed")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { gameState = "menu" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Icon(Icons.Rounded.Replay, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
