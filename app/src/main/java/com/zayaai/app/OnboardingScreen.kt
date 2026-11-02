package com.zayaai.app

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // =================================================================
            // Top Bar: AI ZAYA Brand Pill (Left) & Skip Button (Right)
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official AI ZAYA Wordmark & Z Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF5F3FF))
                        .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_zaya_brand_z),
                        contentDescription = "AI ZAYA",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI ZAYA",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF1E1B4B)
                    )
                }

                // Skip Button (Visible on all slides)
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onComplete()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // =================================================================
            // Middle: Horizontal Pager for 3 Slides
            // =================================================================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingSlideContent(
                        title = "Everything Starts with AI ZAYA",
                        description = "Discover a smarter way to learn, create, write, code and get things done."
                    ) {
                        WorkspaceIllustration()
                    }
                    1 -> OnboardingSlideContent(
                        title = "All Your Tools, One Place",
                        description = "Explore AI chat, image creation, coding, writing, study tools and productivity features."
                    ) {
                        ToolsDashboardIllustration()
                    }
                    2 -> OnboardingSlideContent(
                        title = "Make Every Idea Count",
                        description = "Start exploring powerful tools designed to help you learn, create and achieve more."
                    ) {
                        CreativeProductivityIllustration()
                    }
                }
            }

            // =================================================================
            // Bottom Bar: 3 Pagination Indicators + Primary Action Button
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 Pagination Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(durationMillis = 250),
                            label = "indicator_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF7C3AED)
                                    else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }

                // Primary Capsule Button
                val isLastPage = pagerState.currentPage == 2
                val buttonText = if (isLastPage) "Get Started →" else "Next →"

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
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF6D28D9),
                                        Color(0xFF7C3AED),
                                        Color(0xFF8B5CF6)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Slide Container with Consistent Layout & Margins
 */
@Composable
private fun OnboardingSlideContent(
    title: String,
    description: String,
    illustration: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = (-0.5).sp
            ),
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle / Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

// =============================================================================
// Clean, Modern, Human-Designed Product Illustrations
// =============================================================================

/**
 * Slide 1: Modern Minimalist Workspace (Laptop + Code/Doc blocks)
 */
@Composable
private fun WorkspaceIllustration() {
    Box(
        modifier = Modifier
            .size(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp, 160.dp)) {
            val w = size.width
            val h = size.height

            // Laptop Base
            drawRoundRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(w * 0.15f, h * 0.78f),
                size = Size(w * 0.70f, h * 0.08f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            // Laptop Screen Bezel
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(w * 0.22f, h * 0.20f),
                size = Size(w * 0.56f, h * 0.58f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            // Laptop Screen Display
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(w * 0.25f, h * 0.24f),
                size = Size(w * 0.50f, h * 0.50f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            // Code lines inside laptop
            val lineColors = listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8), Color(0xFF34D399), Color(0xFFA78BFA))
            val lineLengths = listOf(0.32f, 0.22f, 0.40f, 0.28f)
            for (i in lineColors.indices) {
                drawRoundRect(
                    color = lineColors[i],
                    topLeft = Offset(w * 0.28f, h * (0.30f + i * 0.09f)),
                    size = Size(w * lineLengths[i], 5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }

            // Floating Document Card beside laptop
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.62f, h * 0.10f),
                size = Size(w * 0.32f, h * 0.45f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(w * 0.62f, h * 0.10f),
                size = Size(w * 0.32f, h * 0.45f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
            // Doc Header bar
            drawRoundRect(
                color = Color(0xFF7C3AED),
                topLeft = Offset(w * 0.67f, h * 0.16f),
                size = Size(w * 0.16f, 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            // Doc lines
            for (i in 0..2) {
                drawRoundRect(
                    color = Color(0xFFCBD5E1),
                    topLeft = Offset(w * 0.67f, h * (0.24f + i * 0.08f)),
                    size = Size(w * 0.22f, 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }
        }
    }
}

/**
 * Slide 2: Clean 2x2 Dashboard of Representative Tools
 */
@Composable
private fun ToolsDashboardIllustration() {
    Box(
        modifier = Modifier
            .size(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF8FAFC))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolBadgeCard(
                    title = "AI Chat",
                    icon = Icons.Rounded.ChatBubbleOutline,
                    accent = Color(0xFF7C3AED),
                    bgColor = Color(0xFFEDE9FE),
                    modifier = Modifier.weight(1f)
                )
                ToolBadgeCard(
                    title = "Code Gen",
                    icon = Icons.Rounded.Code,
                    accent = Color(0xFF2563EB),
                    bgColor = Color(0xFFDBEAFE),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolBadgeCard(
                    title = "Image Gen",
                    icon = Icons.Rounded.Image,
                    accent = Color(0xFF9333EA),
                    bgColor = Color(0xFFF3E8FF),
                    modifier = Modifier.weight(1f)
                )
                ToolBadgeCard(
                    title = "Study Hub",
                    icon = Icons.Rounded.School,
                    accent = Color(0xFF059669),
                    bgColor = Color(0xFFD1FAE5),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ToolBadgeCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = Color(0xFF1E293B)
            )
        }
    }
}

/**
 * Slide 3: Inspiring Creativity & Productivity Composition
 */
@Composable
private fun CreativeProductivityIllustration() {
    Box(
        modifier = Modifier
            .size(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            // Elegant subtle dashed orbit
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = size.width * 0.36f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        // Central AI ZAYA Emblem
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDDD6FE)),
            shadowElevation = 3.dp,
            modifier = Modifier.size(68.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_zaya_brand_z),
                    contentDescription = "Z Logo",
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Orbital Nodes: Lightbulb, Code, Text, Target
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Top Node: Lightbulb (Ideas)
            OrbitalNode(
                icon = Icons.Rounded.Lightbulb,
                accent = Color(0xFFD97706),
                bgColor = Color(0xFFFEF3C7),
                modifier = Modifier.offset(y = (-70).dp)
            )
            // Right Node: Edit / Write
            OrbitalNode(
                icon = Icons.Rounded.EditNote,
                accent = Color(0xFF7C3AED),
                bgColor = Color(0xFFEDE9FE),
                modifier = Modifier.offset(x = 70.dp)
            )
            // Bottom Node: Target (Goals)
            OrbitalNode(
                icon = Icons.Rounded.TrackChanges,
                accent = Color(0xFF059669),
                bgColor = Color(0xFFD1FAE5),
                modifier = Modifier.offset(y = 70.dp)
            )
            // Left Node: Insights
            OrbitalNode(
                icon = Icons.Rounded.Insights,
                accent = Color(0xFF2563EB),
                bgColor = Color(0xFFDBEAFE),
                modifier = Modifier.offset(x = (-70).dp)
            )
        }
    }
}

@Composable
private fun OrbitalNode(
    icon: ImageVector,
    accent: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
        shadowElevation = 2.dp,
        modifier = modifier.size(36.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
