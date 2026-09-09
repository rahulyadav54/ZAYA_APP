package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AITutorScreen(
    viewModel: StudyMaterialViewModel,
    paddingValues: PaddingValues
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages come
    LaunchedEffect(viewModel.tutorMessages.size) {
        if (viewModel.tutorMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.tutorMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.School, null, tint = NeonPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Tutor", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    Text(
                        "Studying: ${viewModel.currentMaterialTitle}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Welcome message if empty
        if (viewModel.tutorMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = NeonPurple, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ask me anything about your study material!",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "I'll answer based on the content you uploaded.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Suggestion chips
                    val suggestions = listOf(
                        "Summarize the key concepts",
                        "Explain the most important topic",
                        "Give me an example",
                        "What should I focus on for exams?"
                    )
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                viewModel.tutorInput = suggestion
                                viewModel.sendTutorMessage()
                            },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            modifier = Modifier.padding(vertical = 2.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = NeonCyan
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = NeonCyan.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        } else {
            // Messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(viewModel.tutorMessages) { (message, isUser) ->
                    ChatBubble(message = message, isUser = isUser)
                }

                if (viewModel.isTutorProcessing) {
                    item {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Tutor is thinking...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Input bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.tutorInput,
                    onValueChange = { viewModel.tutorInput = it },
                    placeholder = { Text("Ask about your material...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = NeonPurple
                    ),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3,
                    enabled = !viewModel.isTutorProcessing
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.sendTutorMessage() },
                    enabled = viewModel.tutorInput.isNotBlank() && !viewModel.isTutorProcessing
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        null,
                        tint = if (viewModel.tutorInput.isNotBlank()) NeonPurple else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = if (isUser) 300.dp else 340.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) NeonPurple.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            if (isUser) {
                Text(
                    message,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            } else {
                MarkdownText(
                    text = message,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 22.sp
                )
            }
        }
    }
}


