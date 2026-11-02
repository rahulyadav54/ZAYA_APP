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
    val bgColor = if (isDark) Color(0xFF0B0C16) else Color(0xFFF8F9FE)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        SidebarHeader(userName, userEmail)
        Spacer(modifier = Modifier.height(4.dp))

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
            SectionTitle("Productivity")
            MenuItem(Screen.PromptEngineer, currentRoute == Screen.PromptEngineer.route) { onItemSelected(Screen.PromptEngineer) }
            MenuItem(Screen.BrainGames, currentRoute == Screen.BrainGames.route) { onItemSelected(Screen.BrainGames) }
            MenuItem(Screen.StudentTools, currentRoute == Screen.StudentTools.route) { onItemSelected(Screen.StudentTools) }
            MenuItem(Screen.PromptLibrary, currentRoute == Screen.PromptLibrary.route) { onItemSelected(Screen.PromptLibrary) }
            MenuItem(Screen.StartupIdea, currentRoute == Screen.StartupIdea.route) { onItemSelected(Screen.StartupIdea) }

            // AI Features
            SectionTitle("Features")
            MenuItem(Screen.Voice, currentRoute == Screen.Voice.route) { onItemSelected(Screen.Voice) }
            MenuItem(Screen.History, currentRoute == Screen.History.route) { onItemSelected(Screen.History) }
        }

        // App Settings
        SectionTitle("General")
        MenuItem(Screen.Settings, currentRoute == Screen.Settings.route) { onItemSelected(Screen.Settings) }
        MenuItem(Screen.About, currentRoute == Screen.About.route) { onItemSelected(Screen.About) }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        NavigationDrawerItem(
            label = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFEF4444)
                )
            },
            selected = false,
            onClick = onLogout,
            icon = {
                Icon(
                    Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFFEF4444)
                )
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Powered by ZAYA CODE HUB",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    val isDark = LocalIsDarkTheme.current
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            fontSize = 11.sp
        ),
        color = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.85f) else Color(0xFF6B21A8),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}
