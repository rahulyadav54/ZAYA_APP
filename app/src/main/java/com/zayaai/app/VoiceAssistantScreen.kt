package com.zayaai.app

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun VoiceAssistantScreen(viewModel: VoiceViewModel, paddingValues: PaddingValues) {
    val isListening by viewModel.isListening.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val voiceState = viewModel.voiceState
    val aiResponse = viewModel.aiResponse

    val config = remember(voiceState) { VoiceAnimations.configFor(voiceState) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    // ─── Animated Values ───
    val infiniteTransition = rememberInfiniteTransition(label = "main")

    val micScale by infiniteTransition.animateFloat(
        initialValue = config.pulseMin,
        targetValue = config.pulseMax,
        animationSpec = infiniteRepeatable(
            animation = tween(config.pulseDuration, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val outerRingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotate"
    )

    // Tap scale animation
    var tapScale by remember { mutableFloatStateOf(1f) }
    val animatedTapScale by animateFloatAsState(
        targetValue = tapScale,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "tap"
    )

    // ─── Full Screen Layout ───
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A18),
                        Color(0xFF12082A),
                        Color(0xFF0A0614),
                        Color(0xFF000000)
                    )
                )
            )
            .padding(paddingValues)
    ) {
        // Layer 1: Floating particles
        FloatingParticles(config = config)

        // Layer 2: Glow ring
        GlowRing(config = config, rmsLevel = rmsLevel)

        // Layer 3: Wave ring animation
        WaveRingAnimation(
            config = config,
            rmsLevel = rmsLevel,
            modifier = Modifier
                .size(440.dp)
                .align(Alignment.Center)
                .graphicsLayer { rotationZ = outerRingRotation * 0.02f }
        )

        // ─── Content ───
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ─── Top Bar ───
            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                if (viewModel.isTeacherMode) "AI VOICE TUTOR" else "VOICE ASSISTANT",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp,
                    fontSize = 17.sp
                ),
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle hint
            Text(
                if (viewModel.isTeacherMode) "Interactive Voice Learning • Ask any question"
                else "Real-Time AI Voice • Speak naturally anytime",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp, fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status pill
            AnimatedContent(
                targetState = when (voiceState) {
                    VoiceState.SPEAKING -> "Speaking • Tap mic to interrupt"
                    VoiceState.LISTENING -> "Listening • Speak now..."
                    VoiceState.PROCESSING -> "Thinking..."
                    VoiceState.IDLE -> "Ready • Tap mic to speak"
                },
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "status"
            ) { text ->
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // State dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (voiceState) {
                                        VoiceState.LISTENING -> Color(0xFF00E5FF)
                                        VoiceState.PROCESSING -> Color(0xFFAD1DEB)
                                        VoiceState.SPEAKING -> Color(0xFF00E676)
                                        VoiceState.IDLE -> Color.White.copy(alpha = 0.3f)
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ─── Center Area: Mic + Waves ───
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // ── Mic Button ──
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(micScale * animatedTapScale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                tapScale = 0.85f
                                when (voiceState) {
                                    VoiceState.LISTENING -> viewModel.stopListening()
                                    VoiceState.SPEAKING -> viewModel.interruptAndListen() // Interrupt & listen
                                    else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glow ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            config.micColors.first().copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                        )

                        // Mid ring
                        Box(
                            modifier = Modifier
                                .size(165.dp)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            config.micColors.first().copy(alpha = 0.4f),
                                            Color.Transparent,
                                            config.micColors.last().copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .graphicsLayer { rotationZ = outerRingRotation }
                        )

                        // Inner solid mic
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(
                                    Brush.linearGradient(config.micColors),
                                    CircleShape
                                )
                                .border(
                                    1.5.dp,
                                    Color.White.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (voiceState) {
                                    VoiceState.LISTENING -> Icons.Rounded.Stop
                                    VoiceState.SPEAKING -> Icons.Rounded.Mic // Tap to interrupt
                                    VoiceState.PROCESSING -> Icons.Rounded.Psychology
                                    else -> Icons.Rounded.Mic
                                },
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Reset tap scale
                    LaunchedEffect(tapScale) {
                        if (tapScale < 1f) {
                            kotlinx.coroutines.delay(100)
                            tapScale = 1f
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Partial / Recognized Text ──
                    AnimatedVisibility(
                        visible = partialText.isNotEmpty() && isListening,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(150))
                    ) {
                        Text(
                            text = partialText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(horizontal = 20.dp)
                        )
                    }

                    // Recognized text (after listening)
                    AnimatedVisibility(
                        visible = recognizedText.isNotEmpty() &&
                                !isListening &&
                                voiceState != VoiceState.IDLE &&
                                recognizedText != "Listening...",
                        enter = fadeIn() + slideInVertically { 20 },
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "\"$recognizedText\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Response preview
                    AnimatedVisibility(
                        visible = aiResponse.isNotEmpty() && voiceState == VoiceState.SPEAKING,
                        enter = fadeIn(tween(400)) + expandVertically(),
                        exit = fadeOut(tween(200))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.06f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = aiResponse,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // ─── Bottom Controls ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear conversation
                BottomControlButton(
                    icon = Icons.Rounded.DeleteSweep,
                    label = "Clear",
                    enabled = viewModel.conversationHistory.isNotEmpty(),
                    onClick = { viewModel.clearConversation() }
                )

                // Teacher mode toggle
                BottomControlButton(
                    icon = Icons.Rounded.School,
                    label = if (viewModel.isTeacherMode) "Tutor ON" else "Tutor OFF",
                    isActive = viewModel.isTeacherMode,
                    onClick = { viewModel.toggleTeacherMode() }
                )

                // Auto-listen toggle
                BottomControlButton(
                    icon = Icons.Rounded.Loop,
                    label = if (viewModel.isContinuousMode) "Auto ON" else "Auto OFF",
                    isActive = viewModel.isContinuousMode,
                    onClick = { viewModel.toggleContinuousMode() }
                )

                // Memory count
                BottomControlButton(
                    icon = Icons.Rounded.Memory,
                    label = "${viewModel.contextCount / 2} chats",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─── Bottom Control Button ───

@Composable
private fun BottomControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isActive) Color(0xFF00E5FF).copy(alpha = 0.12f)
                    else Color.White.copy(alpha = 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF00E5FF)
                else Color.White.copy(alpha = if (enabled) 0.4f else 0.15f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color(0xFF00E5FF).copy(alpha = 0.8f)
            else Color.White.copy(alpha = if (enabled) 0.35f else 0.15f),
            fontSize = 10.sp
        )
    }
}

// ─── Keep backward compat ───
@Composable
fun RmsWaveformVisualizer(rmsLevel: Float) {
    // Deprecated  —  kept for backward compatibility
}

