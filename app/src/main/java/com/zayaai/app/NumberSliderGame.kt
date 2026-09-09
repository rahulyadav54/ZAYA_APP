package com.zayaai.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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

@Composable
fun NumberSliderScreen(paddingValues: PaddingValues) {
    val isDark = LocalIsDarkTheme.current

    var gameState by remember { mutableStateOf("menu") }
    var moves by remember { mutableIntStateOf(0) }
    var tiles by remember { mutableStateOf(listOf<Int>()) }
    var emptyPos by remember { mutableIntStateOf(15) }
    var isSolved by remember { mutableStateOf(false) }
    var bestMoves by remember { mutableIntStateOf(Int.MAX_VALUE) }
    var elapsedTime by remember { mutableIntStateOf(0) }

    fun initBoard() {
        // Generate a solvable puzzle
        val board = (1..15).toMutableList()
        board.add(0) // 0 = empty
        // Shuffle by making random valid moves
        var empty = 15
        repeat(200) {
            val neighbors = mutableListOf<Int>()
            val row = empty / 4
            val col = empty % 4
            if (row > 0) neighbors.add((row - 1) * 4 + col)
            if (row < 3) neighbors.add((row + 1) * 4 + col)
            if (col > 0) neighbors.add(row * 4 + col - 1)
            if (col < 3) neighbors.add(row * 4 + col + 1)
            val swap = neighbors.random()
            board[empty] = board[swap]
            board[swap] = 0
            empty = swap
        }
        tiles = board
        emptyPos = empty
        moves = 0
        isSolved = false
    }

    fun checkSolved(): Boolean {
        for (i in 0..14) {
            if (tiles[i] != i + 1) return false
        }
        return tiles[15] == 0
    }

    fun moveTile(pos: Int) {
        if (isSolved) return
        val row = pos / 4
        val col = pos % 4
        val emptyRow = emptyPos / 4
        val emptyCol = emptyPos % 4

        if ((row == emptyRow && kotlin.math.abs(col - emptyCol) == 1) ||
            (col == emptyCol && kotlin.math.abs(row - emptyRow) == 1)) {
            val newTiles = tiles.toMutableList()
            newTiles[emptyPos] = newTiles[pos]
            newTiles[pos] = 0
            tiles = newTiles
            emptyPos = pos
            moves++
            if (checkSolved()) {
                isSolved = true
                if (moves < bestMoves) bestMoves = moves
                gameState = "result"
            }
        }
    }

    // Timer
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            elapsedTime = 0
            while (gameState == "playing") {
                kotlinx.coroutines.delay(1000L)
                elapsedTime++
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
                Text("🧩", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "15 Puzzle",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Slide tiles to arrange numbers 1-15 in order.\nThe classic brain teaser!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("How to Play", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(
                            "🔢 Arrange numbers 1 to 15 in order",
                            "👆 Tap a tile next to the empty space",
                            "🎯 Solve in fewest moves possible",
                            "⏱️ Race against your best time!"
                        ).forEach { rule ->
                            Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                if (bestMoves < Int.MAX_VALUE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD600).copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏆", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Best: $bestMoves moves", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        initBoard()
                        gameState = "playing"
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB))
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Puzzle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            "playing" -> {
                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.TouchApp, null, tint = Color(0xFFE040FB), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$moves moves", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Timer, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val mins = elapsedTime / 60
                            val secs = elapsedTime % 60
                            Text("${mins}:${secs.toString().padStart(2, '0')}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    OutlinedButton(
                        onClick = { initBoard() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Puzzle grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isDark) Color(0xFF1A1A2E)
                            else Color(0xFFF0F0F5)
                        )
                        .padding(8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        for (row in 0..3) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                for (col in 0..3) {
                                    val index = row * 4 + col
                                    val value = tiles[index]

                                    if (value == 0) {
                                        // Empty space
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isDark) Color(0xFF0F0F1A).copy(alpha = 0.5f)
                                                    else Color(0xFFE0E0E8)
                                                )
                                        )
                                    } else {
                                        // Is tile in correct position?
                                        val isCorrect = value == index + 1
                                        val tileColor = when {
                                            isCorrect -> Color(0xFF00E676).copy(alpha = if (isDark) 0.3f else 0.2f)
                                            isDark -> Color(0xFF2A2A4E)
                                            else -> Color.White
                                        }
                                        val borderColor = if (isCorrect) Color(0xFF00E676).copy(alpha = 0.5f) else Color.Transparent

                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = tileColor),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null,
                                            onClick = { moveTile(index) }
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "$value",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isCorrect) Color(0xFF00E676)
                                                    else MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hint text
                Text(
                    "💡 Green numbers are in the correct position",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            "result" -> {
                Spacer(modifier = Modifier.height(40.dp))
                Text("🏆", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Puzzle Solved!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        val mins = elapsedTime / 60
                        val secs = elapsedTime % 60
                        ResultStatRow("🔢", "Moves", "$moves")
                        ResultStatRow("⏱️", "Time", "${mins}:${secs.toString().padStart(2, '0')}")
                        ResultStatRow("🏆", "Best", if (bestMoves < Int.MAX_VALUE) "$bestMoves moves" else "-")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { gameState = "menu" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB))
                ) {
                    Icon(Icons.Rounded.Replay, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
