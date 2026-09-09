package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun AdminDashboardScreen(paddingValues: PaddingValues, onNavigate: (Screen) -> Unit, onLogout: () -> Unit) {
    val adminTools = listOf(
        AdminTool("Quizzes", Icons.Rounded.Quiz, Screen.QuizManagement, NeonCyan),
        AdminTool("Formulas", Icons.Rounded.Functions, Screen.FormulaManagement, NeonPurple),
        AdminTool("Announcements", Icons.Rounded.Campaign, Screen.AnnouncementManagement, Color(0xFFFFD600)),
        AdminTool("Articles", Icons.AutoMirrored.Rounded.Article, Screen.ArticleManagement, Color(0xFF00E676)),
        AdminTool("Study Materials", Icons.Rounded.Folder, Screen.AdminStudyMaterials, Color(0xFFFF9800))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text("Manage Content", color = NeonCyan, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(adminTools) { tool ->
                AdminToolCard(tool) { onNavigate(tool.screen) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text("Admin Logout", color = Color.Red)
        }
    }
}

data class AdminTool(val title: String, val icon: ImageVector, val screen: Screen, val color: Color)

@Composable
fun AdminToolCard(tool: AdminTool, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tool.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(tool.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

