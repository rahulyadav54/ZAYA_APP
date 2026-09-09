package com.zayaai.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun UploadStudyMaterialScreen(
    viewModel: StudyMaterialViewModel,
    paddingValues: PaddingValues,
    onNavigateToMaterial: (Long) -> Unit
) {
    val context = LocalContext.current
    var materialTitle by remember { mutableStateOf("") }
    var manualText by remember { mutableStateOf("") }
    var selectedFileType by remember { mutableStateOf("") }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedFileType = "pdf"
            viewModel.extractTextFromPdf(context, it)
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedFileType = "image"
            viewModel.extractTextFromImage(context, it)
        }
    }

    val textFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            selectedFileType = "text"
            viewModel.extractTextFromTextFile(context, it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Title Input
        Text("Material Title", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = materialTitle,
            onValueChange = { materialTitle = it },
            placeholder = { Text("e.g. Chapter 5 - Photosynthesis", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Upload Options
        Text("Choose Input Method", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UploadOptionCard(
                icon = Icons.Rounded.PictureAsPdf,
                label = "PDF",
                color = Color(0xFFFF5252),
                modifier = Modifier.weight(1f),
                onClick = { pdfLauncher.launch(arrayOf("application/pdf")) }
            )
            UploadOptionCard(
                icon = Icons.Rounded.Image,
                label = "Image",
                color = Color(0xFF6E72FC),
                modifier = Modifier.weight(1f),
                onClick = { imageLauncher.launch(arrayOf("image/*")) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UploadOptionCard(
                icon = Icons.Rounded.Description,
                label = "Text File",
                color = Color(0xFF00E676),
                modifier = Modifier.weight(1f),
                onClick = { textFileLauncher.launch(arrayOf("text/plain")) }
            )
            UploadOptionCard(
                icon = Icons.Rounded.Edit,
                label = "Paste Text",
                color = Color(0xFFFFD600),
                modifier = Modifier.weight(1f),
                onClick = { selectedFileType = "manual" }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Manual text input
        AnimatedVisibility(visible = selectedFileType == "manual") {
            Column {
                Text("Paste Your Study Content", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = manualText,
                    onValueChange = {
                        manualText = it
                        viewModel.setManualText(it)
                    },
                    placeholder = { Text("Paste your study material here...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 15
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Extracted text preview
        if (viewModel.extractedText.isNotBlank() && selectedFileType != "manual") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Text Extracted Successfully", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.extractedText.take(500) + if (viewModel.extractedText.length > 500) "..." else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 8
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error message
        viewModel.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1515)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ErrorOutline, null, tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(error, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Processing indicator
        if (viewModel.isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(viewModel.processingStep, color = NeonCyan, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Process Button
        Button(
            onClick = {
                viewModel.processStudyMaterial(
                    title = materialTitle,
                    fileType = selectedFileType.ifBlank { "manual" }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            enabled = viewModel.extractedText.isNotBlank() && !viewModel.isProcessing
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (viewModel.extractedText.isNotBlank() && !viewModel.isProcessing)
                                listOf(NeonPurple, NeonCyan)
                            else listOf(Color.Gray, Color.DarkGray)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate AI Learning Content", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // After processing: show navigation to generated content
        viewModel.currentMaterialId?.let { materialId ->
            if (!viewModel.isProcessing && viewModel.generatedQuestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Content Generated!", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))

                GeneratedContentCard(
                    icon = Icons.AutoMirrored.Rounded.HelpOutline,
                    title = "Important Questions",
                    count = "${viewModel.generatedQuestions.size} questions",
                    color = Color(0xFFFF9800),
                    onClick = { onNavigateToMaterial(materialId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                GeneratedContentCard(
                    icon = Icons.Rounded.Quiz,
                    title = "AI Quiz",
                    count = "${viewModel.quizQuestions.size} questions",
                    color = Color(0xFFFF5252),
                    onClick = { onNavigateToMaterial(materialId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                GeneratedContentCard(
                    icon = Icons.Rounded.Style,
                    title = "Flashcards",
                    count = "${viewModel.flashcards.size} cards",
                    color = NeonCyan,
                    onClick = { onNavigateToMaterial(materialId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                GeneratedContentCard(
                    icon = Icons.Rounded.Summarize,
                    title = "Study Summary",
                    count = "Ready",
                    color = Color(0xFF00E676),
                    onClick = { onNavigateToMaterial(materialId) }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun UploadOptionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun GeneratedContentCard(
    icon: ImageVector,
    title: String,
    count: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Text(count, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


