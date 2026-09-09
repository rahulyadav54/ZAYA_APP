package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme

@Composable
fun DrawerContent(
    userName: String,
    userEmail: String,
    userRole: String,
    currentRoute: String?,
    onItemSelected: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(
                if (isDark) Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0F1A), Color(0xFF1A0F2E), Color(0xFF000000))
                ) else Brush.verticalGradient(
                    // use theme-aware surface colors for light mode so contrast is preserved
                    colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Pass email so header can display it and avoid unused-parameter warnings
        SidebarHeader(userName, userEmail)

        Spacer(modifier = Modifier.height(8.dp))

        if (userRole == "admin") {
            SectionTitle("Admin Management")
            MenuItem(Screen.AdminDashboard, currentRoute == Screen.AdminDashboard.route) { onItemSelected(Screen.AdminDashboard) }
            MenuItem(Screen.QuizManagement, currentRoute == Screen.QuizManagement.route) { onItemSelected(Screen.QuizManagement) }
            MenuItem(Screen.FormulaManagement, currentRoute == Screen.FormulaManagement.route) { onItemSelected(Screen.FormulaManagement) }
            MenuItem(Screen.AnnouncementManagement, currentRoute == Screen.AnnouncementManagement.route) { onItemSelected(Screen.AnnouncementManagement) }
            MenuItem(Screen.ArticleManagement, currentRoute == Screen.ArticleManagement.route) { onItemSelected(Screen.ArticleManagement) }
            MenuItem(Screen.AdminStudyMaterials, currentRoute == Screen.AdminStudyMaterials.route) { onItemSelected(Screen.AdminStudyMaterials) }
        } else {
            // AI Study Tools
            SectionTitle("AI Study")
            MenuItem(Screen.UploadStudyMaterial, currentRoute == Screen.UploadStudyMaterial.route) { onItemSelected(Screen.UploadStudyMaterial) }

            // Main Tools
            SectionTitle("Main Tools")
            MenuItem(Screen.AIChat, currentRoute == Screen.AIChat.route) { onItemSelected(Screen.AIChat) }
            MenuItem(Screen.ImageGenerator, currentRoute == Screen.ImageGenerator.route) { onItemSelected(Screen.ImageGenerator) }
            MenuItem(Screen.CodeGenerator, currentRoute == Screen.CodeGenerator.route) { onItemSelected(Screen.CodeGenerator) }
            MenuItem(Screen.TextWriter, currentRoute == Screen.TextWriter.route) { onItemSelected(Screen.TextWriter) }
            MenuItem(Screen.QuizGenerator, currentRoute == Screen.QuizGenerator.route) { onItemSelected(Screen.QuizGenerator) }
            MenuItem(Screen.PPTGenerator, currentRoute == Screen.PPTGenerator.route) { onItemSelected(Screen.PPTGenerator) }
            MenuItem(Screen.DocumentAnalyzer, currentRoute == Screen.DocumentAnalyzer.route) { onItemSelected(Screen.DocumentAnalyzer) }

            // Productivity
            SectionTitle("Productivity Tools")
            MenuItem(Screen.PromptEngineer, currentRoute == Screen.PromptEngineer.route) { onItemSelected(Screen.PromptEngineer) }
            MenuItem(Screen.BrainGames, currentRoute == Screen.BrainGames.route) { onItemSelected(Screen.BrainGames) }
            MenuItem(Screen.StudentTools, currentRoute == Screen.StudentTools.route) { onItemSelected(Screen.StudentTools) }
            MenuItem(Screen.PromptLibrary, currentRoute == Screen.PromptLibrary.route) { onItemSelected(Screen.PromptLibrary) }
            MenuItem(Screen.StartupIdea, currentRoute == Screen.StartupIdea.route) { onItemSelected(Screen.StartupIdea) }

            // AI Features
            SectionTitle("AI Features")
            MenuItem(Screen.Voice, currentRoute == Screen.Voice.route) { onItemSelected(Screen.Voice) }
            MenuItem(Screen.History, currentRoute == Screen.History.route) { onItemSelected(Screen.History) }
        }

        // App Settings
        SectionTitle("App Settings")
        MenuItem(Screen.Settings, currentRoute == Screen.Settings.route) { onItemSelected(Screen.Settings) }
        MenuItem(Screen.About, currentRoute == Screen.About.route) { onItemSelected(Screen.About) }
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))
        
        NavigationDrawerItem(
            label = { Text("Logout", color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    val isDark = LocalIsDarkTheme.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF23233C), // darker for light mode
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}
