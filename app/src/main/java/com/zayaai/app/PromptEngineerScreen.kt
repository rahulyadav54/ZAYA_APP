package com.zayaai.app

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

@Composable
fun PromptEngineerScreen(viewModel: PromptEngineerViewModel, paddingValues: PaddingValues) {
    val context = LocalContext.current
    var userInput by remember { mutableStateOf("") }
    var isOptimizeMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ─── Header ───
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.AutoFixHigh,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "AI Prompt Engineer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Generate & optimize powerful AI prompts instantly",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Mode Toggle ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeTab("Generate", !isOptimizeMode) { isOptimizeMode = false }
            ModeTab("Optimize", isOptimizeMode) { isOptimizeMode = true }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Category Selector (only for Generate mode) ───
        if (!isOptimizeMode) {
            Text(
                "Category",
                style = MaterialTheme.typography.titleSmall,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.categories.forEach { category ->
                    CategoryChip(
                        text = category,
                        isSelected = viewModel.selectedCategory == category,
                        onClick = { viewModel.setCategory(category) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ─── Input Field ───
        Text(
            if (isOptimizeMode) "Paste Your Prompt" else "Describe Your Goal",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            placeholder = {
                Text(
                    if (isOptimizeMode)
                        "Paste an existing prompt to optimize..."
                    else
                        "e.g. Create a realistic photo of a sunset over mountains...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
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

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Generate / Optimize Button ───
        GradientButton(
            text = if (viewModel.isLoading) "Generating..." else if (isOptimizeMode) "Optimize Prompt" else "Generate Prompts",
            isLoading = viewModel.isLoading,
            onClick = {
                if (isOptimizeMode) viewModel.optimizePrompt(userInput)
                else viewModel.generatePrompts(userInput)
            }
        )

        // ─── Error Message ───
        if (viewModel.errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
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

        // ─── Results ───
        val r = viewModel.result
        val hasResults = r.basicPrompt.isNotBlank() || r.advancedPrompt.isNotBlank() ||
                r.proPrompt.isNotBlank() || r.enhancementTips.isNotBlank()

        AnimatedVisibility(
            visible = hasResults,
            enter = fadeIn() + slideInVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(28.dp))

                if (r.basicPrompt.isNotBlank()) {
                    PromptResultCard(
                        label = "âš¡ Basic Prompt",
                        labelColor = Color(0xFF4CAF50),
                        content = r.basicPrompt,
                        onCopy = {
                            viewModel.copyToClipboard(context, r.basicPrompt, "Basic Prompt")
                            Toast.makeText(context, "Basic Prompt copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (r.advancedPrompt.isNotBlank()) {
                    PromptResultCard(
                        label = "ðŸš€ Advanced Prompt",
                        labelColor = Color(0xFF2196F3),
                        content = r.advancedPrompt,
                        onCopy = {
                            viewModel.copyToClipboard(context, r.advancedPrompt, "Advanced Prompt")
                            Toast.makeText(context, "Advanced Prompt copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (r.proPrompt.isNotBlank()) {
                    PromptResultCard(
                        label = "ðŸ‘‘ Pro Prompt",
                        labelColor = Color(0xFFFF9800),
                        content = r.proPrompt,
                        onCopy = {
                            viewModel.copyToClipboard(context, r.proPrompt, "Pro Prompt")
                            Toast.makeText(context, "Pro Prompt copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (r.enhancementTips.isNotBlank()) {
                    PromptResultCard(
                        label = "ðŸ’¡ Enhancement Tips",
                        labelColor = NeonCyan,
                        content = r.enhancementTips,
                        onCopy = {
                            viewModel.copyToClipboard(context, r.enhancementTips, "Tips")
                            Toast.makeText(context, "Tips copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ─── Clear Button ───
                OutlinedButton(
                    onClick = {
                        viewModel.clear()
                        userInput = ""
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

@Composable
private fun ModeTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgModifier = if (isSelected) {
        Modifier.background(
            brush = Brush.horizontalGradient(listOf(NeonPurple, NeonCyan)),
            shape = RoundedCornerShape(10.dp)
        )
    } else {
        Modifier.background(Color.Transparent, RoundedCornerShape(10.dp))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(bgModifier)
            .clickable { onClick() }
            .padding(horizontal = 40.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgModifier = if (isSelected) {
        Modifier.background(
            brush = Brush.horizontalGradient(listOf(NeonPurple, NeonCyan)),
            shape = RoundedCornerShape(20.dp)
        )
    } else {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(bgModifier)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PromptResultCard(
    label: String,
    labelColor: Color,
    content: String,
    onCopy: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = labelColor
            )
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    tint = labelColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    RoundedCornerShape(14.dp)
                )
                .border(
                    1.dp,
                    labelColor.copy(alpha = 0.2f),
                    RoundedCornerShape(14.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 22.sp
            )
        }
    }
}




