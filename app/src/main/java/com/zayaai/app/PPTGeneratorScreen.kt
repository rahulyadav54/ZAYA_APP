package com.zayaai.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

@Composable
fun PPTGeneratorScreen(viewModel: PPTGeneratorViewModel, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var topic by remember { mutableStateOf("") }
    var slideCount by remember { mutableStateOf("10") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // â”€â”€â”€ Header â”€â”€â”€
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Slideshow,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "AI Presentation Generator",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Generate professional PowerPoint presentations with AI",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // â”€â”€â”€ Topic Input â”€â”€â”€
        Text(
            "Presentation Topic",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            placeholder = {
                Text(
                    "e.g. Artificial Intelligence in Healthcare, Climate Change Solutions...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // â”€â”€â”€ Slide Count â”€â”€â”€
        Text(
            "Number of Slides",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = slideCount,
            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) slideCount = it },
            placeholder = { Text("10", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.width(100.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // â”€â”€â”€ Generate Button â”€â”€â”€
        GradientButton(
            text = if (viewModel.isGenerating) "Generating..." else "Generate Presentation",
            isLoading = viewModel.isGenerating,
            onClick = {
                val count = slideCount.toIntOrNull() ?: 10
                viewModel.generatePresentation(topic, count)
            }
        )

        // â”€â”€â”€ Error Message â”€â”€â”€
        if (viewModel.errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x33FF5252), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    viewModel.errorMessage,
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // â”€â”€â”€ Generated Outline â”€â”€â”€
        AnimatedVisibility(
            visible = viewModel.generatedOutline.isNotBlank(),
            enter = fadeIn() + slideInVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "ðŸ“Š Presentation Outline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            NeonCyan.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    MarkdownText(
                        text = viewModel.generatedOutline,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // â”€â”€â”€ Download PPT Button â”€â”€â”€
                Button(
                    onClick = { viewModel.downloadPPT(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !viewModel.isDownloading && viewModel.pptFileUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(NeonPurple, NeonCyan)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isDownloading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Downloading...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Download PPT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // â”€â”€â”€ Download Progress â”€â”€â”€
                if (viewModel.isDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = NeonCyan,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }

                // â”€â”€â”€ Open File Button (after download) â”€â”€â”€
                AnimatedVisibility(
                    visible = viewModel.downloadComplete,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Success card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0x3300E676),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                "âœ… File saved to Downloads/AI_ZAYA/${viewModel.downloadedFileName}",
                                color = Color(0xFF00E676),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.openDownloadedFile(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FolderOpen,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Open File",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // â”€â”€â”€ Clear Button â”€â”€â”€
                OutlinedButton(
                    onClick = {
                        viewModel.clear()
                        topic = ""
                        slideCount = "10"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


