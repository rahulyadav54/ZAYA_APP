package com.zayaai.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.PrimaryPurple
import com.zayaai.app.ui.theme.SecondaryIndigo

/**
 * Image Creation Screen — Coming Soon Experience
 * Image generation is temporarily disabled in AI ZAYA while safety and moderation
 * systems undergo a complete overhaul.
 */
@Composable
fun ImageGeneratorScreen(
    viewModel: ImageGeneratorViewModel,
    paddingValues: PaddingValues,
    onNavigateBack: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── Header ───
        Text(
            text = "Image Creation",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ─── Hero Icon Container ───
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = if (isDark) 0.35f else 0.20f),
                            SecondaryIndigo.copy(alpha = if (isDark) 0.15f else 0.08f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = if (isDark) Color(0xFF1E1F38) else Color(0xFFEDE9FE),
                border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.5f)),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = "Image Creation",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Status Pill: COMING SOON ───
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = PrimaryPurple.copy(alpha = if (isDark) 0.22f else 0.12f),
            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COMING SOON",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = PrimaryPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ─── Main Heading ───
        Text(
            text = "Something Creative Is Coming!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Description ───
        Text(
            text = "We're working on a safer and more powerful image creation experience. This feature will be available in a future update.",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                fontSize = 14.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ─── Small Supporting Text ───
        Text(
            text = "We're improving the experience to make creativity safer and better.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Preview Cards: What We Are Building ───
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureHighlightCard(
                icon = Icons.Rounded.Shield,
                title = "Advanced Safety & Moderation",
                description = "Strict multi-layered guardrails ensuring a safe, respectful, family-friendly creative workspace.",
                isDark = isDark
            )

            FeatureHighlightCard(
                icon = Icons.Rounded.AutoAwesome,
                title = "Enhanced Creative Quality",
                description = "Modern generative architecture with sharper artistic fidelity, diverse styles, and prompt precision.",
                isDark = isDark
            )

            FeatureHighlightCard(
                icon = Icons.Rounded.School,
                title = "Built for Students & Creators",
                description = "Visual concepts and design assets directly optimized for academic presentations and study materials.",
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Notification / Status Card ───
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color(0xFF16182B) else Color(0xFFF3F4F6),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF272A4E) else Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Status",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Image generation is temporarily offline for safety upgrades. All other AI tools remain fully operational.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun FeatureHighlightCard(
    icon: ImageVector,
    title: String,
    description: String,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF141527) else Color(0xFFFFFFFF),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF232544) else Color(0xFFEDE9FE)
        ),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryPurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
