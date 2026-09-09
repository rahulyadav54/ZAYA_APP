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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    var startAnimations by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        startAnimations = true
        delay(2000) // Reduced from 3.5 seconds to 2 seconds
        onSplashComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020205)),
        contentAlignment = Alignment.Center
    ) {
        // Background particles effect
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(15) { index ->
                val xAnim = remember { Animatable(0f) }
                
                LaunchedEffect(Unit) {
                    while(true) {
                        xAnim.animateTo(
                            targetValue = (Math.random() * 1000).toFloat(),
                            animationSpec = tween(durationMillis = 5000 + index * 100, easing = LinearEasing)
                        )
                        xAnim.snapTo(0f)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .offset(x = (index * 30).dp, y = (index * 50).dp)
                        .size(2.dp)
                        .background(NeonCyan.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        // Orbiting rings for futuristic feel
        Box(contentAlignment = Alignment.Center) {
            OrbitRing(size = 220.dp, rotation = rotation, color = NeonCyan.copy(alpha = 0.15f))
            OrbitRing(size = 280.dp, rotation = -rotation * 0.5f, color = NeonPurple.copy(alpha = 0.1f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1.2f))

            // New Logo Integration
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect behind logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .blur(40.dp)
                        .background(NeonPurple.copy(alpha = glowAlpha), CircleShape)
                )
                
                // Outer rotating border
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .background(
                            Brush.sweepGradient(listOf(NeonPurple, NeonCyan, NeonPurple)),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .background(Color(0xFF020205), CircleShape)
                )

                // The actual Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_zaya_logo),
                    contentDescription = "AI ZAYA Logo",
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Attractive Title
            AnimatedVisibility(
                visible = startAnimations,
                enter = fadeIn(tween(1200)) + slideInVertically(tween(1200)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AI ZAYA",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 10.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Empowering Your Learning Journey",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = NeonCyan.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Branding
            AnimatedVisibility(
                visible = startAnimations,
                enter = fadeIn(tween(2000, delayMillis = 500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Text(
                        text = "Developed by",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Zaya Code Hub",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Elegant loading bar
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(120.dp)
                            .height(2.dp),
                        color = NeonCyan,
                        trackColor = Color.White.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }
}

@Composable
fun OrbitRing(size: androidx.compose.ui.unit.Dp, rotation: Float, color: Color) {
    Canvas(modifier = Modifier
        .size(size)
        .graphicsLayer { rotationZ = rotation }) {
        drawCircle(
            color = color,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Satellite dot
        drawCircle(
            color = NeonCyan,
            radius = 3.dp.toPx(),
            center = Offset(size.toPx() / 2, 0f)
        )
    }
}

