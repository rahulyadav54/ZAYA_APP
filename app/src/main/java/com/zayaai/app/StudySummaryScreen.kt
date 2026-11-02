package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun StudySummaryScreen(
    viewModel: StudyMaterialViewModel,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Study Summary",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp
        )
        Text(
            viewModel.currentMaterialTitle,
            color = NeonCyan,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (viewModel.studySummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Summarize, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No summary generated yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            // Parse and render the summary nicely
            val summary = viewModel.studySummary ?: ""
            val lines = summary.split("\n")

            lines.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.isBlank() -> {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    trimmed.startsWith("**") && trimmed.endsWith("**") -> {
                        // Bold header
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            trimmed.removeSurrounding("**"),
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    trimmed.startsWith(" • ") || trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                        // Bullet point
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(" • ", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                trimmed.removePrefix(" • ").removePrefix("-").removePrefix("*").trim(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    trimmed.startsWith("#") -> {
                        // Markdown heading
                        val headingText = trimmed.trimStart('#').trim()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            headingText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    else -> {
                        Text(
                            trimmed,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}


