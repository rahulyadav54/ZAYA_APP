package com.zayaai.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val badge: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector?,
    val useBrandLogo: Boolean = false,
    val accentColors: List<Color>
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val slides = remember {
        listOf(
            OnboardingSlide(
                badge = "AI Intelligence",
                title = "Your Futuristic AI Assistant",
                subtitle = "Experience next-generation conversational AI, smart code generation, and deep document analysis all in one seamless hub.",
                icon = null,
                useBrandLogo = true,
                accentColors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF06B6D4))
            ),
            OnboardingSlide(
                badge = "Academic Powerhouse",
                title = "Supercharge Your Studies",
                subtitle = "Integrated scientific calculator, smart unit converter, interactive quiz generator, and personalized study planners tailored for you.",
                icon = Icons.Rounded.School,
                accentColors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFF6366F1))
            ),
            OnboardingSlide(
                badge = "Real-Time Voice",
                title = "Conversational Voice Tutor",
                subtitle = "Speak naturally with AI ZAYA. Practice languages, brainstorm ideas, and receive instant spoken guidance anytime, anywhere.",
                icon = Icons.Rounded.Mic,
                accentColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF06B6D4))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF080811),
                Color(0xFF140D2B),
                Color(0xFF080811)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF9F9FE),
                Color(0xFFF0EDFF),
                Color(0xFFFFFFFF)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Official Brand Pill & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official AI ZAYA Mini Logo Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color(0xFF1F1D36) else Color(0xFFECEBFF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF3D366B) else Color(0xFFD4CFFE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_brand_logo),
                            contentDescription = "AI ZAYA Logo",
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI ZAYA",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color(0xFF4338CA)
                        )
                    }
                }

                // Skip Button (hidden on final slide)
                if (pagerState.currentPage < slides.size - 1) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onComplete()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustration Badge Box
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient Glow
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            slide.accentColors.first().copy(alpha = if (isDark) 0.35f else 0.22f),
                                            slide.accentColors[1].copy(alpha = if (isDark) 0.15f else 0.10f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        if (slide.useBrandLogo) {
                            // Official Brand Logo
                            Image(
                                painter = painterResource(id = R.drawable.ic_brand_logo),
                                contentDescription = "AI ZAYA Logo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(32.dp))
                            )
                        } else {
                            // Professional Vector Feature Icon
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = RoundedCornerShape(32.dp),
                                color = if (isDark) Color(0xFF18152B) else Color.White,
                                shadowElevation = if (isDark) 0.dp else 8.dp,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    Brush.linearGradient(slide.accentColors)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(
                                                Brush.linearGradient(slide.accentColors),
                                                shape = RoundedCornerShape(22.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = slide.icon ?: Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = slide.accentColors.first().copy(alpha = if (isDark) 0.20f else 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            slide.accentColors.first().copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = slide.badge.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = if (isDark) NeonCyan else slide.accentColors.first(),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 34.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subtitle
                    Text(
                        text = slide.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp
                        ),
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Navigation Area: Page Indicators & Primary Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val widthAnim by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "dot_width"
                        )
                        val color = if (isSelected) {
                            if (isDark) NeonCyan else Color(0xFF6366F1)
                        } else {
                            if (isDark) Color(0xFF2E2A4D) else Color(0xFFD1D5DB)
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(widthAnim)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Continue / Get Started Button (Professional Arrow, NO rocket)
                val isLastPage = pagerState.currentPage == slides.size - 1

                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isLastPage) {
                                        listOf(Color(0xFF7C3AED), Color(0xFF6366F1))
                                    } else {
                                        listOf(Color(0xFF6366F1), Color(0xFF7C3AED))
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isLastPage) "Get Started" else "Continue",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
