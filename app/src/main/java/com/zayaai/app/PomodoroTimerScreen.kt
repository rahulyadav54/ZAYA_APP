package com.zayaai.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerScreen(viewModel: PomodoroViewModel, paddingValues: PaddingValues) {

    val isDark = LocalIsDarkTheme.current

    // -- Theme-aware colors --
    val timerTextColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
    val cardBackground = if (isDark)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark)
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    // -- Mode accent colors --
    val modeAccent by animateColorAsState(
        targetValue = when (viewModel.currentMode) {
            TimerMode.Focus -> NeonPurple
            TimerMode.ShortBreak -> Color(0xFF4CAF50)
            TimerMode.LongBreak -> NeonCyan
        }, label = "modeAccent"
    )
    val modeAccentSecondary by animateColorAsState(
        targetValue = when (viewModel.currentMode) {
            TimerMode.Focus -> NeonCyan
            TimerMode.ShortBreak -> Color(0xFF81C784)
            TimerMode.LongBreak -> NeonPurple
        }, label = "modeAccentSecondary"
    )

    // -- Pulsing glow animation when running --
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // -- Mode selector chips --
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimerMode.entries.forEach { mode ->
                val selected = viewModel.currentMode == mode
                val chipColor by animateColorAsState(
                    if (selected) modeAccent else Color.Transparent,
                    label = "chip"
                )
                val textColor by animateColorAsState(
                    if (selected) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    label = "chipText"
                )
                Surface(
                    onClick = { viewModel.switchMode(mode) },
                    shape = RoundedCornerShape(12.dp),
                    color = chipColor,
                    modifier = Modifier.height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // -- Timer ring with glow --
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(300.dp)
        ) {
            // Glow ring behind (only when running)
            if (viewModel.isRunning) {
                Canvas(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(pulseScale)
                        .alpha(glowAlpha)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                modeAccent.copy(alpha = 0.6f),
                                modeAccentSecondary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                }
            }

            // Background track
            Canvas(modifier = Modifier.size(250.dp)) {
                drawArc(
                    color = if (isDark) Color.Gray.copy(alpha = 0.15f)
                    else Color.Gray.copy(alpha = 0.12f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )

                // Progress arc
                val sweep = 360f * viewModel.progress
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(modeAccent, modeAccentSecondary, modeAccent)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Center content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = viewModel.currentMode.label.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = modeAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.formatTime(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = timerTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (viewModel.isRunning) "Running…" else "Paused",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = subtitleColor
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // -- Control buttons --
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            FilledTonalIconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDark)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Play / Stop
            FloatingActionButton(
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier.size(80.dp),
                containerColor = modeAccent,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = if (viewModel.isRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = if (viewModel.isRunning) "Stop" else "Start",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Skip to next mode
            FilledTonalIconButton(
                onClick = {
                    val next = when (viewModel.currentMode) {
                        TimerMode.Focus -> TimerMode.ShortBreak
                        TimerMode.ShortBreak -> TimerMode.Focus
                        TimerMode.LongBreak -> TimerMode.Focus
                    }
                    viewModel.switchMode(next)
                },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDark)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Skip",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // -- Session tracker --
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            border = CardDefaults.outlinedCardBorder().takeIf { !isDark }
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sessions Completed",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Pomodoro dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.completedSessions == 0) {
                        Text(
                            text = "No sessions yet — start your first focus! ??",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val dotsToShow = viewModel.completedSessions.coerceAtMost(8)
                        repeat(dotsToShow) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(modeAccent, modeAccentSecondary))
                                    )
                            )
                        }
                        if (viewModel.completedSessions > 8) {
                            Text(
                                text = "+${viewModel.completedSessions - 8}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = modeAccent
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -- Motivational quote --
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark)
                    modeAccent.copy(alpha = 0.08f)
                else
                    modeAccent.copy(alpha = 0.06f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.FormatQuote,
                    contentDescription = null,
                    tint = modeAccent.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = viewModel.currentQuote,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

