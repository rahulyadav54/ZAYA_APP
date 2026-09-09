package com.zayaai.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@Composable
fun MemoryMatrixScreen(paddingValues: PaddingValues) {
    val isDark = LocalIsDarkTheme.current

    var gameState by remember { mutableStateOf("menu") } // menu, showing, playing, result
    var gridSize by remember { mutableIntStateOf(3) }
    var level by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var pattern by remember { mutableStateOf(listOf<Int>()) }
    var revealed by remember { mutableStateOf(setOf<Int>()) }
    var correctGuesses by remember { mutableIntStateOf(0) }
    var totalCells by remember { mutableIntStateOf(0) }

    // Calculate cells to highlight based on level
    fun cellsToHighlight(): Int = when {
        level <= 3 -> 3 + level
        level <= 6 -> 5 + level
        else -> 7 + level
    }.coerceAtMost(gridSize * gridSize - 1)

    // Generate pattern
    fun generatePattern() {
        val size = gridSize * gridSize
        val count = cellsToHighlight()
        totalCells = count
        pattern = (0 until size).shuffled().take(count)
        correctGuesses = 0
        revealed = emptySet()
    }

    // Show pattern then hide
    LaunchedEffect(gameState) {
        if (gameState == "showing") {
            delay(1500L + (level * 200L).coerceAtMost(2000L))
            gameState = "playing"
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
                Text("🧠", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Memory Matrix",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Remember the highlighted tiles and tap them!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Grid size selector
                Text("Grid Size", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(3, 4, 5).forEach { size ->
                        FilterChip(
                            selected = gridSize == size,
                            onClick = { gridSize = size },
                            label = { Text("${size}×$size", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        level = 1
                        score = 0
                        lives = 3
                        generatePattern()
                        gameState = "showing"
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF448AFF))
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            "showing", "playing" -> {
                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📊", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Level $level", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
                            repeat(lives) {
                                Text("❤️", fontSize = 14.sp)
                            }
                            repeat(3 - lives) {
                                Text("🖤", fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (gameState == "showing") {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF448AFF).copy(alpha = 0.15f))
                    ) {
                        Text(
                            "👀 Memorize the pattern!",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF448AFF)
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NeonCyan.copy(alpha = 0.15f))
                    ) {
                        Text(
                            "🎯 Tap the highlighted tiles! ($correctGuesses/$totalCells)",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        for (row in 0 until gridSize) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                for (col in 0 until gridSize) {
                                    val index = row * gridSize + col
                                    val isPattern = index in pattern
                                    val isRevealed = index in revealed

                                    val bgColor = when {
                                        gameState == "showing" && isPattern -> Color(0xFF448AFF)
                                        isRevealed && isPattern -> Color(0xFF00E676)
                                        isRevealed && !isPattern -> Color(0xFFFF5252).copy(alpha = 0.5f)
                                        else -> if (isDark) Color(0xFF2A2A3E) else Color(0xFFE8E8F0)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bgColor)
                                            .then(
                                                if (gameState == "playing" && !isRevealed) {
                                                    Modifier.clickable {
                                                        revealed = revealed + index
                                                        if (isPattern) {
                                                            correctGuesses++
                                                            score += 10
                                                            if (correctGuesses >= totalCells) {
                                                                // Level complete!
                                                                level++
                                                                score += level * 20
                                                                if (level % 3 == 0 && gridSize < 6) {
                                                                    gridSize++
                                                                }
                                                                generatePattern()
                                                                gameState = "showing"
                                                            }
                                                        } else {
                                                            lives--
                                                            if (lives <= 0) {
                                                                gameState = "result"
                                                            }
                                                        }
                                                    }
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isRevealed && isPattern) {
                                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        } else if (isRevealed && !isPattern) {
                                            Text("✗", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "result" -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text(if (level >= 5) "🏆" else "🧠", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (level >= 5) "Amazing Memory!" else if (level >= 3) "Good Job!" else "Keep Training!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ResultStatRow("📊", "Score", "$score points")
                        ResultStatRow("📶", "Level Reached", "$level")
                        ResultStatRow("🔢", "Grid Size", "${gridSize}×$gridSize")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { gameState = "menu" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF448AFF))
                ) {
                    Icon(Icons.Rounded.Replay, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
