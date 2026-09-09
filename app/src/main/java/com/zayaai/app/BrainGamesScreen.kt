package com.zayaai.app

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

data class BrainGame(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val screen: Screen,
    val emoji: String,
    val difficulty: String
)

@Composable
fun BrainGamesScreen(
    paddingValues: PaddingValues,
    onNavigate: (Screen) -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    val games = listOf(
        BrainGame("Math Challenge", "Speed math with levels", Icons.Rounded.Calculate, Color(0xFF00E676), Screen.MathChallenge, "🧮", "Easy-Hard"),
        BrainGame("Memory Matrix", "Remember the pattern", Icons.Rounded.GridView, Color(0xFF448AFF), Screen.MemoryMatrix, "🧠", "Medium"),
        BrainGame("Word Scramble", "Unscramble the words", Icons.Rounded.TextRotationNone, Color(0xFFFF9800), Screen.WordScramble, "🔤", "Easy-Medium"),
        BrainGame("15 Puzzle", "Slide tiles in order", Icons.Rounded.ViewModule, Color(0xFFE040FB), Screen.NumberSlider, "🧩", "Hard"),
        BrainGame("Emoji Match", "Find matching pairs", Icons.Rounded.EmojiEmotions, Color(0xFFFFD600), Screen.EmojiMatch, "🎯", "Easy"),
        BrainGame("Color Rush", "Test your reflexes", Icons.Rounded.Palette, Color(0xFFFF5252), Screen.ColorRush, "🎨", "Medium")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            NeonPurple.copy(alpha = if (isDark) 0.3f else 0.15f),
                            NeonCyan.copy(alpha = if (isDark) 0.3f else 0.15f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.4f), NeonCyan.copy(alpha = 0.4f))),
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "🎮",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Brain Games",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Train your brain offline • No internet needed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Games Grid
        val rows = games.chunked(2)
        rows.forEach { rowGames ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowGames.forEach { game ->
                    Box(modifier = Modifier.weight(1f)) {
                        BrainGameCard(game = game, onClick = { onNavigate(game.screen) })
                    }
                }
                if (rowGames.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun BrainGameCard(game: BrainGame, onClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "game_float")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isDark) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    game.color.copy(alpha = if (isDark) 0.6f else 0.4f),
                    if (isDark) Color.Transparent else game.color.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(game.color.copy(alpha = 0.06f), Color.Transparent)
                            )
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .graphicsLayer { translationY = yOffset }
                            .background(
                                game.color.copy(alpha = if (isDark) 0.15f else 0.12f),
                                CircleShape
                            )
                            .border(1.dp, game.color.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(game.emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Difficulty badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(game.color.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            game.difficulty,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = game.color
                        )
                    }
                }

                Column {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
