package com.zayaai.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

private enum class ResumeTab(val label: String, val icon: ImageVector) {
    Templates("Templates", Icons.Rounded.Dashboard),
    Builder("AI Builder", Icons.Rounded.AutoAwesome),
    Analyzer("Analyzer", Icons.Rounded.Analytics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeBuilderScreen(viewModel: ResumeViewModel, paddingValues: PaddingValues) {
    var selectedTab by remember { mutableStateOf(ResumeTab.Templates) }
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // â”€â”€ Tab bar â”€â”€
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ResumeTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val bgColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "tabBg"
                )
                val txtColor by animateColorAsState(
                    if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    label = "tabTxt"
                )
                Surface(
                    onClick = { selectedTab = tab },
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(tab.icon, null, tint = txtColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(tab.label, style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ), color = txtColor)
                    }
                }
            }
        }

        // â”€â”€ Tab content â”€â”€
        when (selectedTab) {
            ResumeTab.Templates -> TemplatesTab(viewModel, isDark)
            ResumeTab.Builder -> BuilderTab(viewModel, isDark)
            ResumeTab.Analyzer -> AnalyzerTab(viewModel, isDark)
        }
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
//  TAB 1: TEMPLATES
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

@Composable
private fun TemplatesTab(viewModel: ResumeViewModel, isDark: Boolean) {
    var selectedTemplate by remember { mutableStateOf<ResumeTemplate?>(null) }
    var userDetails by remember { mutableStateOf("") }
    val templateContent by viewModel.templateContent.collectAsState()
    val isGenerating by viewModel.isGeneratingTemplate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        if (selectedTemplate == null) {
            // â”€â”€ Template gallery â”€â”€
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose a Template",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Pick a style, add your details, and get a ready-to-use resume",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            viewModel.templates.forEach { template ->
                TemplateCard(template, isDark) { selectedTemplate = template }
                Spacer(Modifier.height(12.dp))
            }
        } else {
            // â”€â”€ Fill template â”€â”€
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    selectedTemplate = null
                    viewModel.clearTemplate()
                    userDetails = ""
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Column {
                    Text(selectedTemplate!!.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(selectedTemplate!!.style + " Style",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Sections: ${selectedTemplate!!.sections.joinToString(" Â· ")}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = userDetails,
                onValueChange = { userDetails = it },
                placeholder = { Text("Paste your details hereâ€¦\n\nName, email, phone, education, skills, experience, projects, etc.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(Modifier.height(16.dp))

            GradientButton(
                text = if (isGenerating) "Generatingâ€¦" else "Generate Resume",
                isLoading = isGenerating,
                onClick = { viewModel.generateFromTemplate(selectedTemplate!!, userDetails) }
            )

            if (templateContent.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                ResultCard(title = "Your Resume", content = templateContent, isDark = isDark)
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun TemplateCard(template: ResumeTemplate, isDark: Boolean, onClick: () -> Unit) {
    val accentColor = Color(template.color)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (!isDark) CardDefaults.outlinedCardBorder() else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Description, null, tint = accentColor, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface)
                Text(template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp)
                Spacer(Modifier.height(6.dp))
                Text(template.style,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = accentColor)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
//  TAB 2: AI BUILDER
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

@Composable
private fun BuilderTab(viewModel: ResumeViewModel, isDark: Boolean) {
    var details by remember { mutableStateOf("") }
    val generatedResume by viewModel.generatedResume.collectAsState()
    val isGenerating by viewModel.isGeneratingResume.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("AI Resume Builder",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Paste your details â†’ Get an ATS-friendly resume",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hint chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ðŸ“ Name & Contact", "ðŸŽ“ Education", "ðŸ’¼ Experience", "ðŸ›  Skills").forEach {
                SuggestionChip(
                    onClick = {},
                    label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(20.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            placeholder = {
                Text("Enter your details hereâ€¦\n\n" +
                    "Example:\n" +
                    "Name: John Doe\n" +
                    "Email: john@gmail.com\n" +
                    "Education: B.Tech CSE, XYZ University, 2024\n" +
                    "Skills: Python, React, Firebase\n" +
                    "Experience: Intern at ABC Corp (6 months)\n" +
                    "Projects: E-commerce app, Chat bot",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodySmall)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(Modifier.height(16.dp))

        GradientButton(
            text = if (isGenerating) "Building Resumeâ€¦" else "Generate ATS Resume",
            isLoading = isGenerating,
            onClick = { viewModel.generateResume(details) }
        )

        if (generatedResume.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            ResultCard(title = "Your ATS-Friendly Resume", content = generatedResume, isDark = isDark)

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.clearResume(); details = "" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Over")
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
//  TAB 3: RESUME ANALYZER
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

@Composable
private fun AnalyzerTab(viewModel: ResumeViewModel, isDark: Boolean) {
    var resumeText by remember { mutableStateOf("") }
    val analysisResult by viewModel.analysisResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val isExtracting by viewModel.isExtracting.collectAsState()
    val uploadedFileName by viewModel.uploadedFileName.collectAsState()
    val uploadedFileType by viewModel.uploadedFileType.collectAsState()
    val extractedResumeText by viewModel.extractedResumeText.collectAsState()
    val context = LocalContext.current

    // Sync extracted text into the text field
    LaunchedEffect(extractedResumeText) {
        if (extractedResumeText.isNotBlank()) {
            resumeText = extractedResumeText
        }
    }

    // Document picker (PDF, TXT, DOC, CSV, etc.)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadResumeFile(uri, context)
        }
    }

    // Image picker (for image-based resumes / screenshots)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadResumeFile(uri, context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Analytics, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Resume Analyzer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Upload your resume or paste text to analyze",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Feature chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ðŸ“Š Score", "ðŸŽ¯ Job Match", "ðŸ”‘ Keywords").forEach {
                SuggestionChip(
                    onClick = {},
                    label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(20.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // â”€â”€ Upload Section â”€â”€
        Text(
            "Upload Resume",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))

        // Upload buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Document upload button
            UploadOptionCard(
                icon = Icons.Rounded.Description,
                title = "Document",
                subtitle = "PDF, DOC, TXT",
                gradientColors = listOf(Color(0xFF6E72FC), Color(0xFF9D50BB)),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = {
                    documentPickerLauncher.launch(arrayOf(
                        "application/pdf",
                        "text/plain",
                        "text/csv",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/html",
                        "application/json",
                        "application/rtf"
                    ))
                }
            )

            // Image upload button
            UploadOptionCard(
                icon = Icons.Rounded.Image,
                title = "Image",
                subtitle = "JPG, PNG, Screenshot",
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFF5722)),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
        }

        // â”€â”€ Extracting indicator â”€â”€
        AnimatedVisibility(
            visible = isExtracting,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E1E30) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "Extracting text from your resume...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // â”€â”€ Uploaded file preview â”€â”€
        AnimatedVisibility(
            visible = uploadedFileName != null && !isExtracting,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1A2A1A) else Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF66BB6A)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (uploadedFileType) {
                                "image" -> Icons.Rounded.Image
                                "pdf" -> Icons.Rounded.PictureAsPdf
                                else -> Icons.AutoMirrored.Rounded.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            uploadedFileName ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "âœ… Text extracted successfully",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.clearUploadedFile()
                            resumeText = ""
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // â”€â”€ Divider with "OR" â”€â”€
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                "  OR PASTE TEXT  ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = resumeText,
            onValueChange = { resumeText = it },
            placeholder = {
                Text("Paste your complete resume text hereâ€¦\n\n" +
                    "Copy everything from your resume (name, contact, education, skills, experience, etc.) " +
                    "and paste it here. The AI will analyze it and suggest matching jobs.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    style = MaterialTheme.typography.bodySmall)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF9800),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(Modifier.height(16.dp))

        GradientButton(
            text = if (isAnalyzing) "Analyzingâ€¦" else "Analyze My Resume",
            isLoading = isAnalyzing,
            onClick = { viewModel.analyzeResume(resumeText) }
        )

        if (analysisResult.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            ResultCard(title = "Analysis Report", content = analysisResult, isDark = isDark)

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    viewModel.clearAnalysis()
                    viewModel.clearUploadedFile()
                    resumeText = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Analyze Another")
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

// â”€â”€â”€ Upload Option Card â”€â”€â”€

@Composable
private fun UploadOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (!isDark) CardDefaults.outlinedCardBorder() else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
//  SHARED COMPONENTS
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

@Composable
private fun ResultCard(title: String, content: String, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (!isDark) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Description, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(16.dp))
            MarkdownText(
                text = content,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}


