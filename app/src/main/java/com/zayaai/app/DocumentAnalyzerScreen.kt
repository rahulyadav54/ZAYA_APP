package com.zayaai.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun DocumentAnalyzerScreen(viewModel: DocumentAnalyzerViewModel, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var customQuestion by remember { mutableStateOf("") }
    var showPasteDialog by remember { mutableStateOf(false) }
    var pasteText by remember { mutableStateOf("") }

    // File pickers
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.extractFromPdf(context, it) }
    }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.extractFromImage(context, it) }
    }
    val textFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.extractFromTextFile(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // â”€â”€â”€ Header â”€â”€â”€
        Text(
            "Document Analyzer",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Upload a document and let AI analyze, summarize, or answer questions about it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // â”€â”€â”€ Upload Section â”€â”€â”€
        Text(
            "Upload Document",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UploadCard(
                icon = Icons.Rounded.PictureAsPdf,
                label = "PDF",
                color = Color(0xFFFF5252),
                modifier = Modifier.weight(1f),
                onClick = { pdfLauncher.launch(arrayOf("application/pdf")) }
            )
            UploadCard(
                icon = Icons.Rounded.Image,
                label = "Image",
                color = Color(0xFF6E72FC),
                modifier = Modifier.weight(1f),
                onClick = { imageLauncher.launch(arrayOf("image/*")) }
            )
            UploadCard(
                icon = Icons.Rounded.Description,
                label = "Text",
                color = Color(0xFF00E676),
                modifier = Modifier.weight(1f),
                onClick = { textFileLauncher.launch(arrayOf("text/plain")) }
            )
            UploadCard(
                icon = Icons.Rounded.ContentPaste,
                label = "Paste",
                color = Color(0xFFFFD600),
                modifier = Modifier.weight(1f),
                onClick = { showPasteDialog = true }
            )
        }

        // â”€â”€â”€ Extracting Indicator â”€â”€â”€
        if (viewModel.isExtracting) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeonCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Extracting text from ${viewModel.fileType}...",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // â”€â”€â”€ Error Message â”€â”€â”€
        viewModel.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Error, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // â”€â”€â”€ Extracted Text Preview â”€â”€â”€
        AnimatedVisibility(visible = viewModel.extractedText.isNotBlank()) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                // File info bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (viewModel.fileType) {
                            "PDF" -> Icons.Rounded.PictureAsPdf
                            "Image" -> Icons.Rounded.Image
                            "Text" -> Icons.Rounded.Description
                            else -> Icons.Rounded.Edit
                        },
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            viewModel.fileName,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp
                        )
                        Text(
                            "${viewModel.extractedText.length} chars · ${viewModel.extractedText.split("\\s+".toRegex()).size} words",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    // Copy extracted text
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("text", viewModel.extractedText))
                            Toast.makeText(context, "Text copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    // Clear
                    IconButton(
                        onClick = { viewModel.clear() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }

                // Extracted text preview (collapsed)
                Spacer(modifier = Modifier.height(8.dp))
                var showFullText by remember { mutableStateOf(false) }
                val previewText = if (showFullText) viewModel.extractedText
                    else viewModel.extractedText.take(500) + if (viewModel.extractedText.length > 500) "..." else ""

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Extracted Text",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            previewText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                        if (viewModel.extractedText.length > 500) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (showFullText) "Show less ▴" else "Show more ▾",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { showFullText = !showFullText }
                            )
                        }
                    }
                }

                // â”€â”€â”€ Analysis Tools â”€â”€â”€
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Analyze Document",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Analysis option chips - Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalysisChip("Summary", Modifier.weight(1f)) { viewModel.analyzeDocument("summary") }
                    AnalysisChip("Key Points", Modifier.weight(1f)) { viewModel.analyzeDocument("key_points") }
                    AnalysisChip("Explain", Modifier.weight(1f)) { viewModel.analyzeDocument("explain") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalysisChip("Questions", Modifier.weight(1f)) { viewModel.analyzeDocument("questions") }
                    AnalysisChip("Outline", Modifier.weight(1f)) { viewModel.analyzeDocument("outline") }
                    AnalysisChip("Critique", Modifier.weight(1f)) { viewModel.analyzeDocument("critique") }
                }

                // Custom question
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customQuestion,
                    onValueChange = { customQuestion = it },
                    placeholder = { Text("Ask a question about this document...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (customQuestion.isNotBlank()) {
                                    viewModel.askQuestion(customQuestion)
                                }
                            },
                            enabled = customQuestion.isNotBlank() && !viewModel.isAnalyzing
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Ask",
                                tint = if (customQuestion.isNotBlank()) NeonCyan else Color.Gray
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    singleLine = true
                )
            }
        }

        // â”€â”€â”€ Analyzing Indicator â”€â”€â”€
        if (viewModel.isAnalyzing) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeonCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "AI is analyzing your document...",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // â”€â”€â”€ Analysis Result â”€â”€â”€
        AnimatedVisibility(
            visible = viewModel.analysisResult.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Analysis Result",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonCyan
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("analysis", viewModel.analysisResult))
                            Toast.makeText(context, "Analysis copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(14.dp)) {
                        MarkdownText(
                            text = viewModel.analysisResult,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // â”€â”€â”€ Paste Text Dialog â”€â”€â”€
    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = { Text("Paste Document Text") },
            text = {
                OutlinedTextField(
                    value = pasteText,
                    onValueChange = { pasteText = it },
                    placeholder = { Text("Paste your document text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pasteText.isNotBlank()) {
                            viewModel.setManualText(pasteText)
                            pasteText = ""
                        }
                        showPasteDialog = false
                    }
                ) {
                    Text("Analyze", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// â”€â”€â”€ Upload Option Card â”€â”€â”€
@Composable
private fun UploadCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark)
                color.copy(alpha = 0.1f)
            else
                color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 2.dp
        ),
        shape = RoundedCornerShape(14.dp),
        border = if (!isDark) androidx.compose.foundation.BorderStroke(
            0.5.dp, color.copy(alpha = 0.2f)
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                color = if (isDark) color else color.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// â”€â”€â”€ Analysis Chip â”€â”€â”€
@Composable
private fun AnalysisChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        color = if (isDark) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isDark) 0.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            NeonCyan.copy(alpha = if (isDark) 0.2f else 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}


