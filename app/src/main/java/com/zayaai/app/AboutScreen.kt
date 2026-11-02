package com.zayaai.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ContactSupport
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
import com.zayaai.app.ui.theme.*

@Composable
fun AboutScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo & Header
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF5F3FF)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_zaya_brand_z),
                contentDescription = "AI ZAYA Brand Logo",
                modifier = Modifier.size(76.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "AI ZAYA",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "ALL-IN-ONE AI TOOLS",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = NeonPurple
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Powered by ZAYA CODE HUB",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = NeonPurple
        )

        Spacer(modifier = Modifier.height(32.dp))

        // App Description
        AboutSectionCard(title = "App Description", icon = Icons.Rounded.Description) {
            Text(
                text = "AI ZAYA is an intelligent productivity and learning application developed by Zaya Code Hub. The app provides AI tools, student productivity utilities, and smart digital tools for students and professionals.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }

        // App Information
        AboutSectionCard(title = "App Information", icon = Icons.Rounded.Info) {
            InfoRow(label = "App Name", value = "AI ZAYA")
            InfoRow(label = "Version", value = "2.2")
            InfoRow(label = "Category", value = "AI Productivity & Learning")
            InfoRow(label = "Platform", value = "Android")
        }

        // Developer Information
        AboutSectionCard(title = "Developer Information", icon = Icons.Rounded.Person) {
            InfoRow(label = "Developer", value = "Rahul Yadav")
            InfoRow(label = "Role", value = "Founder & Developer")
            InfoRow(label = "Company", value = "Zaya Code Hub")
        }

        // Company Information
        AboutSectionCard(title = "Company Information", icon = Icons.Rounded.Business) {
            Text(
                text = "Zaya Code Hub is a software development company focusing on AI solutions, mobile applications, and web technologies.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Features
        AboutSectionCard(title = "Features", icon = Icons.Rounded.Star) {
            val features = listOf(
                "AI Chat Assistant",
                "Voice AI Assistant",
                "Student Tools",
                "Offline Study Tools",
                "Productivity Utilities"
            )
            features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feature, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Contact
        AboutSectionCard(title = "Contact", icon = Icons.Rounded.ContactSupport) {
            InfoRow(label = "Email", value = "zayacodehub@gmail.com")
            InfoRow(label = "Location", value = "Tamil Nadu, India")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Â© 2026 Zaya Code Hub. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AboutSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    val isDark = LocalIsDarkTheme.current
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
            Icon(icon, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = NeonCyan
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            NeonPurple.copy(alpha = if (isDark) 0.2f else 0.3f),
                            NeonCyan.copy(alpha = if (isDark) 0.2f else 0.3f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
