package com.zayaai.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToolsScreen(paddingValues: PaddingValues) {
    var text by remember { mutableStateOf("") }
    
    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val charCount = text.length
    val sentenceCount = if (text.isBlank()) 0 else text.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Word Counter & Text Formatter", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("Enter or paste your text here...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("Words", wordCount.toString(), Modifier.weight(1f))
            StatCard("Chars", charCount.toString(), Modifier.weight(1f))
            StatCard("Sentences", sentenceCount.toString(), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Formatting Tools", color = NeonCyan, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Replaced FlowRow with a horizontally scrollable Row to avoid NoSuchMethodError
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { text = text.uppercase() },
                label = { Text("UPPERCASE", color = MaterialTheme.colorScheme.onBackground) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), labelColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            )
            AssistChip(
                onClick = { text = text.lowercase() },
                label = { Text("lowercase", color = MaterialTheme.colorScheme.onBackground) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), labelColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            )
            AssistChip(
                onClick = { 
                    text = text.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
                },
                label = { Text("Capitalize", color = MaterialTheme.colorScheme.onBackground) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), labelColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            )
            AssistChip(
                onClick = { text = text.replace("\\s+".toRegex(), " ") },
                label = { Text("Remove Extra Spaces", color = MaterialTheme.colorScheme.onBackground) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), labelColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            )
            AssistChip(
                onClick = { text = "" },
                label = { Text("Clear All", color = Color.Red.copy(alpha = 0.7f)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), labelColor = Color.Red.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(value, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

