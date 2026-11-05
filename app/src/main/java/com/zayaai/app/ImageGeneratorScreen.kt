package com.zayaai.app

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

@Composable
fun ImageGeneratorScreen(viewModel: ImageGeneratorViewModel, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current

    // Convert raw bytes to ImageBitmap for display
    val imageBitmap = remember(viewModel.generatedImageBytes) {
        viewModel.generatedImageBytes?.let { bytes ->
            try {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) { null }
        }
    }

    // In-App Reporting Dialog (Mandated by Google Play AI Policy)
    if (showReportDialog) {
        ReportContentDialog(
            contentSnippet = viewModel.lastPrompt,
            contentType = "Image",
            onDismiss = { showReportDialog = false },
            onSubmitReport = { reason, details ->
                viewModel.submitReport(reason, details)
                showReportDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "What image should I generate?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = {
                Text(
                    "Describe the image (e.g. A sunset over mountains, futuristic city...)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Community Policy Note
        Text(
            "Family-friendly & educational. Sexually explicit content and nudity are strictly prohibited.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Generate / Cancel buttons
        if (viewModel.isImageLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientButton(
                    text = "Generating...",
                    isLoading = true,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { viewModel.cancelGeneration() },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancel", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel")
                }
            }
        } else {
            GradientButton(
                text = "Generate Image",
                isLoading = false,
                onClick = { viewModel.generateImage(prompt) }
            )
        }

        // Loading indicator with elapsed time
        if (viewModel.isImageLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            ImageLoadingCard(status = viewModel.loadingStatus)
        }

        // Error message (Safety rejection / connection error)
        if (viewModel.errorMessage.isNotBlank() && !viewModel.isImageLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33FF5252), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        viewModel.errorMessage,
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.retry() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Report feedback notice
        if (viewModel.isReported) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        viewModel.reportFeedback,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Generated image display
        if (imageBitmap != null && !viewModel.isImageLoading) {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Generated Image",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) NeonCyan else MaterialTheme.colorScheme.primary
                )

                // Visible In-App Report Button (Google Play Policy Compliance)
                TextButton(
                    onClick = { showReportDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = "Report Image",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Report",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Display the image directly from bytes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (isDark) NeonCyan.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "AI Generated Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        viewModel.clear()
                        prompt = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                OutlinedButton(
                    onClick = { viewModel.retry() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Regenerate", color = NeonCyan)
                }

                Button(
                    onClick = { viewModel.downloadImage(context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !viewModel.isDownloading,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    if (viewModel.isDownloading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Loading card with animated dots + elapsed time ───

@Composable
private fun ImageLoadingCard(status: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "imgLoad")
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )
    val dots = ".".repeat(dotCount.toInt())

    var elapsedSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            elapsedSeconds++
        }
    }
    LaunchedEffect(status) { elapsedSeconds = 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "$status$dots",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${elapsedSeconds}s elapsed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
