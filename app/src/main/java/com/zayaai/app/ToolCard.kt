package com.zayaai.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.zayaai.app.ui.theme.LocalIsDarkTheme

@Composable
fun ToolCard(tool: Tool, onClick: () -> Unit) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "icon_float")
    val iconYOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Card(
        modifier = Modifier
            .aspectRatio(0.82f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 4.dp
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isDark) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    tool.color.copy(alpha = if (isDark) 0.6f else 0.4f),
                    if (isDark) Color.Transparent else tool.color.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle gradient overlay at top for light mode
            if (!isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    tool.color.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer { translationY = iconYOffset }
                        .background(
                            tool.color.copy(alpha = if (isDark) 0.15f else 0.12f),
                            CircleShape
                        )
                        .border(1.dp, tool.color.copy(alpha = if (isDark) 0.3f else 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = tool.color,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

