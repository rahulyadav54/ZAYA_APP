package com.zayaai.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*
import kotlin.random.Random

// ─── Organic Wave Ring ───

@Composable
fun WaveRingAnimation(
    config: VoiceAnimConfig,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_ring")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (4000 / config.waveSpeed).toInt().coerceIn(800, 12000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseRadius = minOf(cx, cy) * 0.38f

        for (layer in 0 until config.waveLayers) {
            val layerFraction = layer.toFloat() / config.waveLayers
            val layerRadius = baseRadius + (layer * 24f) + (breathe * 8f)
            val amplitude = config.waveAmplitude * (1f - layerFraction * 0.35f) * (0.5f + rmsLevel * 0.9f)
            val layerColor = config.waveColors.getOrElse(layer) {
                config.waveColors.last()
            }
            val phaseOffset = layer * 0.7f

            drawOrganicWaveRing(
                cx = cx,
                cy = cy,
                radius = layerRadius,
                amplitude = amplitude * layerRadius,
                phase = phase + phaseOffset,
                color = layerColor,
                strokeWidth = (4f - layer * 0.5f).coerceAtLeast(1.2f)
            )
        }
    }
}

private fun DrawScope.drawOrganicWaveRing(
    cx: Float,
    cy: Float,
    radius: Float,
    amplitude: Float,
    phase: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = Path()
    val steps = 360
    for (i in 0..steps) {
        val angle = (i.toFloat() / steps) * 2f * PI.toFloat()
        // Multiple sine waves for organic feel  —  gentler frequencies for smooth motion
        val wave = amplitude * (
                sin(angle * 3f + phase) * 0.45f +
                        sin(angle * 4f - phase * 1.2f) * 0.3f +
                        sin(angle * 6f + phase * 0.6f) * 0.15f +
                        sin(angle * 2f - phase * 0.8f) * 0.1f
                )
        val r = radius + wave
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

// ─── Floating Particles ───

data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val angle: Float,
    val alpha: Float
)

@Composable
fun FloatingParticles(
    config: VoiceAnimConfig,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                speed = (Random.nextFloat() * 0.3f + 0.1f),
                angle = Random.nextFloat() * 2f * PI.toFloat(),
                alpha = Random.nextFloat() * 0.4f + 0.1f
            )
        }
    }

    val time by rememberInfiniteTransition(label = "particles").animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val speed = config.particleSpeed

        particles.forEach { p ->
            val px = ((p.x * w + cos(p.angle) * time * p.speed * speed) % w + w) % w
            val py = ((p.y * h + sin(p.angle) * time * p.speed * speed * 0.5f) % h + h) % h

            drawCircle(
                color = Color.White.copy(alpha = p.alpha * 0.6f),
                radius = p.size,
                center = Offset(px, py)
            )
        }
    }
}

// ─── Glow Ring (soft radial glow behind mic) ───

@Composable
fun GlowRing(
    config: VoiceAnimConfig,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(config.pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = minOf(cx, cy) * 0.65f
        val intensity = glowPulse * (0.7f + rmsLevel * 0.5f)

        // Outer soft glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    config.glowColor.copy(alpha = config.glowAlpha * intensity),
                    config.glowColor.copy(alpha = config.glowAlpha * 0.4f * intensity),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = maxR
            ),
            center = Offset(cx, cy),
            radius = maxR
        )
    }
}


