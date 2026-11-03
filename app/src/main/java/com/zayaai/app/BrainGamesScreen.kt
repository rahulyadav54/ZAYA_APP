package com.zayaai.app

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ads.BannerAdCard
import com.zayaai.app.ads.RewardedAdManager
import com.zayaai.app.ui.theme.*
import kotlinx.coroutines.launch

data class BrainGameItem(
    val id: String,
    val title: String,
    val category: String,
    val subtitle: String,
    val screen: Screen,
    val emoji: String,
    val badge: String,
    val gradient: List<Color>,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainGamesScreen(
    paddingValues: PaddingValues,
    onNavigate: (Screen) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val dataStoreManager = remember { DataStoreManager(context) }

    val hintsCount by dataStoreManager.brainGameHintsFlow.collectAsState(initial = 5)
    val xpCount by dataStoreManager.brainGameXpFlow.collectAsState(initial = 120)

    val games = remember {
        listOf(
            BrainGameItem(
                id = "math",
                title = "Math Challenge",
                category = "ARITHMETIC",
                subtitle = "Speed calculations & arithmetic reflex",
                screen = Screen.MathChallenge,
                emoji = "🧮",
                badge = "Speed",
                gradient = listOf(Color(0xFF059669), Color(0xFF10B981)),
                accentColor = Color(0xFF10B981)
            ),
            BrainGameItem(
                id = "memory",
                title = "Memory Matrix",
                category = "RECALL",
                subtitle = "Pattern recall & working memory grid",
                screen = Screen.MemoryMatrix,
                emoji = "🧠",
                badge = "Memory",
                gradient = listOf(Color(0xFF4F46E5), Color(0xFF6366F1)),
                accentColor = Color(0xFF6366F1)
            ),
            BrainGameItem(
                id = "scramble",
                title = "Word Scramble",
                category = "VOCABULARY",
                subtitle = "Anagrams & linguistic sharpness",
                screen = Screen.WordScramble,
                emoji = "🔤",
                badge = "Hints Ready",
                gradient = listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
                accentColor = Color(0xFFF59E0B)
            ),
            BrainGameItem(
                id = "puzzle15",
                title = "15 Number Puzzle",
                category = "LOGIC",
                subtitle = "Tile sliding & spatial strategy",
                screen = Screen.NumberSlider,
                emoji = "🧩",
                badge = "Logic",
                gradient = listOf(Color(0xFF7C3AED), Color(0xFF9333EA)),
                accentColor = Color(0xFF9333EA)
            ),
            BrainGameItem(
                id = "emoji",
                title = "Emoji Match",
                category = "OBSERVATION",
                subtitle = "Visual pairs & focus speed",
                screen = Screen.EmojiMatch,
                emoji = "🎯",
                badge = "Focus",
                gradient = listOf(Color(0xFFCA8A04), Color(0xFFEAB308)),
                accentColor = Color(0xFFEAB308)
            ),
            BrainGameItem(
                id = "color_rush",
                title = "Color Rush",
                category = "REFLEX",
                subtitle = "Stroop effect & reflex training",
                screen = Screen.ColorRush,
                emoji = "🎨",
                badge = "Reflex",
                gradient = listOf(Color(0xFFE11D48), Color(0xFFF43F5E)),
                accentColor = Color(0xFFF43F5E)
            )
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Top Hub Stats & Wallet Bar ---
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Brain Games Hub",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Level ${(xpCount / 100) + 1} • Genius Mind",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryPurple
                    )
                }

                // Interactive Hint Wallet Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isDark) Color(0xFF261A45) else Color(0xFFF3E8FF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        PrimaryPurple.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clickable {
                            if (activity != null) {
                                RewardedAdManager.show(
                                    activity = activity,
                                    onUserEarnedReward = { reward ->
                                        scope.launch {
                                            dataStoreManager.addHints(5)
                                            Toast.makeText(context, "🎉 +5 Hints Added to your Wallet!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onAdFailedToShow = {
                                        Toast.makeText(context, "Sponsor ad loading, retry shortly.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$hintsCount",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = PrimaryPurple
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Add Hints",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Featured Daily Sprint Hero Card ---
        item(span = { GridItemSpan(2) }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onNavigate(Screen.MathChallenge) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF6D28D9),
                                    Color(0xFF7C3AED),
                                    Color(0xFF4F46E5)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🔥 DAILY COGNITIVE SPRINT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Train Math & Reflex",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Earn +150 XP and top today's leaderboard",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "Play",
                                    tint = Color(0xFF6D28D9),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Section Title ---
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Brain Workouts",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "6 Offline Games",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 4. Redesigned Game Cards ---
        items(games) { game ->
            RedesignedBrainGameCard(
                game = game,
                isDark = isDark,
                onClick = { onNavigate(game.screen) }
            )
        }

        // --- 5. Rewarded Hints Booster Card ---
        item(span = { GridItemSpan(2) }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF1E1738) else Color(0xFFFAF5FF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF7C3AED).copy(alpha = 0.4f) else Color(0xFFE9D5FF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Need Hints for Games?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Watch a short sponsor video to get +5 Free Hints instantly!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (activity != null) {
                                RewardedAdManager.show(
                                    activity = activity,
                                    onUserEarnedReward = { reward ->
                                        scope.launch {
                                            dataStoreManager.addHints(5)
                                            Toast.makeText(context, "🎉 +5 Hints Added to your Wallet!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    onAdFailedToShow = {
                                        Toast.makeText(context, "Sponsor video loading, retry shortly.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text("Get Hints", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 6. Adaptive Production Banner Ad ---
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(4.dp))
            BannerAdCard()
        }

        // --- Bottom Spacer ---
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RedesignedBrainGameCard(
    game: BrainGameItem,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "game_float")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF16152B) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) DarkBorder.copy(alpha = 0.5f) else LightBorder.copy(alpha = 0.7f)
        ),
        shadowElevation = if (isDark) 0.dp else 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Category & Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.category,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    color = game.accentColor
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = game.accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = game.badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = game.accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Animated Emoji Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer { translationY = bounceY }
                    .clip(RoundedCornerShape(16.dp))
                    .background(game.accentColor.copy(alpha = if (isDark) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.emoji,
                    fontSize = 28.sp
                )
            }

            // Game Title & Subtitle
            Column {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.subtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
