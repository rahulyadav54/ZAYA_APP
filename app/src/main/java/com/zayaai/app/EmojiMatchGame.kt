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
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import kotlinx.coroutines.delay

private val emojiSets = listOf(
    listOf("🐶", "🐱", "🐭", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷"),
    listOf("🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍑", "🥭", "🍍"),
    listOf("⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🏓", "🏸"),
    listOf("🚗", "🚕", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "✈️", "🚀", "🛸", "🚁"),
    listOf("🌸", "🌺", "🌻", "🌹", "🌷", "💐", "🌵", "🍀", "🌿", "🍁", "🌴", "🎋")
)

data class EmojiCard(
    val id: Int,
    val emoji: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun EmojiMatchScreen(paddingValues: PaddingValues) {
    val isDark = LocalIsDarkTheme.current

    var gameState by remember { mutableStateOf("menu") }
    var gridSize by remember { mutableIntStateOf(4) } // 4x3, 4x4, 6x4
    var rows by remember { mutableIntStateOf(3) }
    var cards by remember { mutableStateOf(listOf<EmojiCard>()) }
    var firstFlipped by remember { mutableStateOf<Int?>(null) }
    var secondFlipped by remember { mutableStateOf<Int?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var moves by remember { mutableIntStateOf(0) }
    var matches by remember { mutableIntStateOf(0) }
    var totalPairs by remember { mutableIntStateOf(0) }
    var elapsedTime by remember { mutableIntStateOf(0) }

    fun initGame() {
        val cols = gridSize
        val totalCards = cols * rows
        val pairsNeeded = totalCards / 2
        totalPairs = pairsNeeded
        val emojiSet = emojiSets.random().shuffled().take(pairsNeeded)
        val allEmojis = (emojiSet + emojiSet).shuffled()
        cards = allEmojis.mapIndexed { index, emoji -> EmojiCard(id = index, emoji = emoji) }
        firstFlipped = null
        secondFlipped = null
        isChecking = false
        moves = 0
        matches = 0
    }

    // Check match
    LaunchedEffect(secondFlipped) {
        if (secondFlipped != null && firstFlipped != null) {
            isChecking = true
            delay(600L)
            val f = firstFlipped!!
            val s = secondFlipped!!
            val newCards = cards.toMutableList()
            if (newCards[f].emoji == newCards[s].emoji) {
                newCards[f] = newCards[f].copy(isMatched = true)
                newCards[s] = newCards[s].copy(isMatched = true)
                matches++
                if (matches >= totalPairs) {
                    cards = newCards
                    gameState = "result"
                    return@LaunchedEffect
                }
            } else {
                newCards[f] = newCards[f].copy(isFlipped = false)
                newCards[s] = newCards[s].copy(isFlipped = false)
            }
            cards = newCards
            firstFlipped = null
            secondFlipped = null
            isChecking = false
        }
    }

    // Timer
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            elapsedTime = 0
            while (gameState == "playing") {
                delay(1000L)
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
                Text("🎯", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Emoji Match",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Find matching emoji pairs!\nTest your visual memory.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text("Board Size", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple("Easy", "4×3 (6 pairs)", Pair(4, 3)),
                    Triple("Medium", "4×4 (8 pairs)", Pair(4, 4)),
                    Triple("Hard", "4×5 (10 pairs)", Pair(4, 5))
                ).forEach { (label, desc, size) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (gridSize == size.first && rows == size.second)
                                Color(0xFFFFD600).copy(alpha = if (isDark) 0.2f else 0.12f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (gridSize == size.first && rows == size.second) 
                            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD600)) else null,
                        onClick = { gridSize = size.first; rows = size.second }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = gridSize == size.first && rows == size.second,
                                onClick = { gridSize = size.first; rows = size.second },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD600))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        initGame()
                        gameState = "playing"
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Game", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            "playing" -> {
                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.TouchApp, null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$moves", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$matches/$totalPairs", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
                }

                // Progress
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { matches.toFloat() / totalPairs },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFD600),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until rows) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (col in 0 until gridSize) {
                                    val index = row * gridSize + col
                                    if (index < cards.size) {
                                        val card = cards[index]

                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.85f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when {
                                                    card.isMatched -> Color(0xFF00E676).copy(alpha = 0.15f)
                                                    card.isFlipped -> Color(0xFFFFD600).copy(alpha = if (isDark) 0.2f else 0.12f)
                                                    isDark -> Color(0xFF2A2A4E)
                                                    else -> Color(0xFFE8E8F0)
                                                }
                                            ),
                                            border = when {
                                                card.isMatched -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
                                                card.isFlipped -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.4f))
                                                else -> null
                                            },
                                            elevation = CardDefaults.cardElevation(defaultElevation = if (card.isFlipped || card.isMatched) 0.dp else 2.dp),
                                            onClick = {
                                                if (!isChecking && !card.isFlipped && !card.isMatched) {
                                                    val newCards = cards.toMutableList()
                                                    newCards[index] = card.copy(isFlipped = true)
                                                    cards = newCards

                                                    if (firstFlipped == null) {
                                                        firstFlipped = index
                                                    } else {
                                                        secondFlipped = index
                                                        moves++
                                                    }
                                                }
                                            }
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (card.isFlipped || card.isMatched) {
                                                    Text(card.emoji, fontSize = 28.sp)
                                                } else {
                                                    Text("❓", fontSize = 24.sp)
                                                }
                                            }
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
                Text("🎉", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (moves <= totalPairs + 3) "Perfect Memory!" else if (moves <= totalPairs * 2) "Great Job!" else "Well Done!",
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
                        ResultStatRow("🎯", "Pairs Found", "$totalPairs")
                        ResultStatRow("⭐", "Accuracy", "${((totalPairs.toFloat() / moves) * 100).toInt()}%")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { gameState = "menu" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                ) {
                    Icon(Icons.Rounded.Replay, null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
