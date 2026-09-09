package com.zayaai.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private val loadingMessages = listOf(
    "Authenticating your credentials...",
    "Initializing AI assistant...",
    "Preparing your personalized dashboard...",
    "Loading intelligent features...",
    "Almost ready..."
)

@Composable
fun FuturisticLoadingScreen(onLoadingComplete: () -> Unit) {

    // -- Message rotation --
    var messageIndex by remember { mutableIntStateOf(0) }

    // -- Entry animations --
    var contentVisible by remember { mutableStateOf(false) }

    // -- Infinite transitions --
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    // Outer ring rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "outerRot"
    )

    // Inner ring rotation (opposite direction)
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "innerRot"
    )

    // Pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "pulse"
    )

    // Glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow"
    )

    // Progress arc sweep
    val arcSweep by infiniteTransition.animateFloat(
        initialValue = 30f, targetValue = 300f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutCubic), RepeatMode.Reverse
        ), label = "arc"
    )

    // Particle orbit angle
    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "particle"
    )

    // -- Side-effects --
    LaunchedEffect(Unit) {
        contentVisible = true
        // Rotate messages every 600ms
        for (i in loadingMessages.indices) {
            delay(600L)
            messageIndex = i
        }
        // Hold last message briefly then finish
        delay(400L)
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020205), Color(0xFF0A0A1A), Color(0xFF020205))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // -- Background floating particles --
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val particleCount = 25
            for (i in 0 until particleCount) {
                val seed = i * 137.508f // golden-angle spread
                val radius = (100 + i * 18).toFloat()
                val angle = Math.toRadians((seed + particleAngle * (if (i % 2 == 0) 1 else -1)).toDouble())
                val px = w / 2 + radius * cos(angle).toFloat()
                val py = h / 2 + radius * sin(angle).toFloat()
                val alpha = (0.15f + (i % 5) * 0.06f).coerceAtMost(0.45f)
                val pSize = (1.5f + (i % 4)).dp.toPx()
                drawCircle(
                    color = if (i % 3 == 0) NeonCyan.copy(alpha = alpha)
                    else NeonPurple.copy(alpha = alpha),
                    radius = pSize,
                    center = Offset(px, py)
                )
            }
        }

        // -- Outer futuristic ring --
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer { rotationZ = outerRotation }
        ) {
            drawCircle(
                color = NeonCyan.copy(alpha = 0.08f),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Satellite dots on outer ring
            for (i in 0 until 4) {
                val a = Math.toRadians((i * 90).toDouble())
                val cx = center.x + (size.width / 2) * cos(a).toFloat()
                val cy = center.y + (size.height / 2) * sin(a).toFloat()
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        }

        // -- Middle dashed ring --
        Canvas(
            modifier = Modifier
                .size(210.dp)
                .graphicsLayer { rotationZ = innerRotation }
        ) {
            drawCircle(
                color = NeonPurple.copy(alpha = 0.12f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // -- Center content --
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Animated AI progress arc + Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(pulseScale)
            ) {
                // Glow behind logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .blur(50.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    NeonPurple.copy(alpha = glowAlpha),
                                    NeonCyan.copy(alpha = glowAlpha * 0.5f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Spinning arc (futuristic loader)
                Canvas(modifier = Modifier.size(150.dp).rotate(outerRotation)) {
                    rotate(degrees = outerRotation, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    NeonCyan.copy(alpha = 0.3f),
                                    NeonCyan,
                                    NeonPurple,
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = arcSweep,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Second arc (counter-spin)
                Canvas(modifier = Modifier.size(130.dp).rotate(innerRotation)) {
                    rotate(degrees = innerRotation, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    NeonPurple.copy(alpha = 0.2f),
                                    NeonPurple.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            ),
                            startAngle = 180f,
                            sweepAngle = arcSweep * 0.6f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Logo circle - uses the actual app logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    NeonPurple.copy(alpha = 0.9f),
                                    NeonCyan.copy(alpha = 0.9f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_zaya_logo),
                        contentDescription = "AI ZAYA",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // -- App name --
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 40 }
            ) {
                Text(
                    text = "AI ZAYA",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -- Rotating status messages --
            AnimatedContent(
                targetState = messageIndex,
                transitionSpec = {
                    (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 })
                        .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 2 })
                },
                label = "message"
            ) { index ->
                Text(
                    text = loadingMessages.getOrElse(index) { loadingMessages.last() },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = NeonCyan.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // -- Progress dots --
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                loadingMessages.indices.forEach { i ->
                    val isActive = i <= messageIndex
                    val dotScale by animateFloatAsState(
                        targetValue = if (i == messageIndex) 1.4f else if (isActive) 1f else 0.6f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "dot_$i"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            i == messageIndex -> NeonCyan
                            isActive -> NeonPurple
                            else -> Color.White.copy(alpha = 0.15f)
                        },
                        animationSpec = tween(400),
                        label = "dotColor_$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .background(dotColor, CircleShape)
                    )
                }
            }
        }

        // -- Bottom branding --
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(1200, delayMillis = 400)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Powered by",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Zaya Code Hub",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
