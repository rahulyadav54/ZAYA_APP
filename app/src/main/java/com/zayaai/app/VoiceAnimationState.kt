package com.zayaai.app

import androidx.compose.ui.graphics.Color

/**
 * Animation configuration per VoiceState.
 */
data class VoiceAnimConfig(
    val waveSpeed: Float,
    val waveAmplitude: Float,
    val waveLayers: Int,
    val glowAlpha: Float,
    val pulseMin: Float,
    val pulseMax: Float,
    val pulseDuration: Int,
    val micColors: List<Color>,
    val waveColors: List<Color>,
    val glowColor: Color,
    val statusText: String,
    val particleSpeed: Float
)

object VoiceAnimations {
    private val cyan = Color(0xFF00E5FF)
    private val purple = Color(0xFF9D50BB)
    private val pink = Color(0xFFFF4081)
    private val blue = Color(0xFF6E72FC)
    private val green = Color(0xFF00E676)
    private val white = Color.White

    fun configFor(state: VoiceState): VoiceAnimConfig = when (state) {
        VoiceState.IDLE -> VoiceAnimConfig(
            waveSpeed = 0.3f,
            waveAmplitude = 0.15f,
            waveLayers = 3,
            glowAlpha = 0.08f,
            pulseMin = 0.97f,
            pulseMax = 1.03f,
            pulseDuration = 3000,
            micColors = listOf(purple, blue),
            waveColors = listOf(
                purple.copy(alpha = 0.12f),
                blue.copy(alpha = 0.08f),
                cyan.copy(alpha = 0.05f)
            ),
            glowColor = purple.copy(alpha = 0.15f),
            statusText = "Tap to speak",
            particleSpeed = 0.3f
        )

        VoiceState.LISTENING -> VoiceAnimConfig(
            waveSpeed = 1.0f,
            waveAmplitude = 0.4f,
            waveLayers = 5,
            glowAlpha = 0.3f,
            pulseMin = 0.97f,
            pulseMax = 1.05f,
            pulseDuration = 1400,
            micColors = listOf(cyan, Color(0xFF00BCD4)),
            waveColors = listOf(
                cyan.copy(alpha = 0.35f),
                blue.copy(alpha = 0.25f),
                purple.copy(alpha = 0.18f),
                pink.copy(alpha = 0.12f),
                cyan.copy(alpha = 0.08f)
            ),
            glowColor = cyan.copy(alpha = 0.35f),
            statusText = "Listening...",
            particleSpeed = 0.8f
        )

        VoiceState.PROCESSING -> VoiceAnimConfig(
            waveSpeed = 0.8f,
            waveAmplitude = 0.25f,
            waveLayers = 4,
            glowAlpha = 0.2f,
            pulseMin = 0.96f,
            pulseMax = 1.06f,
            pulseDuration = 1200,
            micColors = listOf(purple, Color(0xFFAD1DEB)),
            waveColors = listOf(
                purple.copy(alpha = 0.3f),
                pink.copy(alpha = 0.2f),
                blue.copy(alpha = 0.15f),
                purple.copy(alpha = 0.08f)
            ),
            glowColor = purple.copy(alpha = 0.25f),
            statusText = "Thinking...",
            particleSpeed = 0.8f
        )

        VoiceState.SPEAKING -> VoiceAnimConfig(
            waveSpeed = 1.0f,
            waveAmplitude = 0.35f,
            waveLayers = 4,
            glowAlpha = 0.2f,
            pulseMin = 0.96f,
            pulseMax = 1.08f,
            pulseDuration = 1000,
            micColors = listOf(green, Color(0xFF00C853)),
            waveColors = listOf(
                green.copy(alpha = 0.28f),
                cyan.copy(alpha = 0.18f),
                blue.copy(alpha = 0.12f),
                green.copy(alpha = 0.06f)
            ),
            glowColor = green.copy(alpha = 0.2f),
            statusText = "Speaking...",
            particleSpeed = 0.6f
        )
    }
}



