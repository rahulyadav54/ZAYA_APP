package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun MenuItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    
    val background = if (isSelected) {
        if (isDark) {
            Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.2f), Color.Transparent))
        } else {
            Brush.horizontalGradient(listOf(NeonPurple.copy(alpha = 0.15f), Color.Transparent))
        }
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val iconColor = if (isSelected) NeonCyan else {
        if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF4A4A5E)
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onBackground
    } else {
        if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF1F1F2E)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = screen.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                ),
                color = textColor
            )
        }
    }
}

