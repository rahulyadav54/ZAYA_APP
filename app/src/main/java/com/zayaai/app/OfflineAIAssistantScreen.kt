package com.zayaai.app

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple
import kotlinx.coroutines.launch

@Composable
fun OfflineAIAssistantScreen(
    viewModel: OfflineAIAssistantViewModel,
    paddingValues: PaddingValues
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    // Auto-scroll
    LaunchedEffect(viewModel.messages.size, viewModel.isTyping) {
        if (viewModel.messages.isNotEmpty()) {
            val target = viewModel.messages.size - 1 + if (viewModel.isTyping) 1 else 0
            try { listState.animateScrollToItem(target) } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
    ) {
        // ─── Header ───
        OfflineAssistantHeader(
            isDark = isDark,
            onClearChat = { viewModel.clearChat() }
        )

        // ─── Messages ───
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.messages, key = { it.timestamp }) { msg ->
                OfflineMessageBubble(message = msg, isDark = isDark)
            }
            if (viewModel.isTyping) {
                item { OfflineTypingIndicator(isDark) }
            }
        }

        // ─── Quick Suggestions ───
        if (viewModel.messages.size <= 1) {
            OfflineSuggestionChips(onSuggestionClick = { suggestion ->
                inputText = ""
                coroutineScope.launch { viewModel.sendMessage(suggestion) }
            })
        }

        // ─── Input Bar ───
        OfflineInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    val text = inputText
                    inputText = ""
                    coroutineScope.launch { viewModel.sendMessage(text) }
                }
            },
            isTyping = viewModel.isTyping,
            isDark = isDark
        )
    }
}

// ─── Header ───

@Composable
private fun OfflineAssistantHeader(isDark: Boolean, onClearChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark)
                    Brush.horizontalGradient(listOf(Color(0xFF1A0F2E), Color(0xFF0F0F1A)))
                else
                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Offline badge avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00BFA5))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.SmartToy, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Offline Assistant",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00E676), CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Always available • No internet needed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00E676)
                )
            }
        }

        IconButton(onClick = onClearChat) {
            Icon(Icons.Rounded.DeleteSweep, "Clear chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Message Bubble ───

@Composable
private fun OfflineMessageBubble(message: OfflineMessage, isDark: Boolean) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI avatar
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00BFA5))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SmartToy, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = if (isUser) 290.dp else 330.dp)
        ) {
            Card(
                shape = if (isUser)
                    RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                else
                    RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = if (!isUser)
                    Modifier.border(
                        1.dp,
                        if (isDark) Color(0xFF00E676).copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
                    )
                else Modifier
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            if (isUser) {
                                if (isDark)
                                    Brush.linearGradient(listOf(Color(0xFF00BFA5).copy(alpha = 0.6f), Color(0xFF00E676).copy(alpha = 0.4f)))
                                else
                                    Brush.linearGradient(listOf(Color(0xFF00BFA5).copy(alpha = 0.85f), Color(0xFF00E676).copy(alpha = 0.7f)))
                            } else {
                                if (isDark)
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, Color(0xFF1A2A1A)))
                                else
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.surface))
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (isUser) {
                        Text(
                            text = message.text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    } else {
                        MarkdownText(
                            text = message.text,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Timestamp
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatOfflineTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // User avatar
        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (isDark) Color(0xFF00BFA5).copy(alpha = 0.5f) else Color(0xFF00BFA5).copy(alpha = 0.75f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── Typing Indicator ───

@Composable
private fun OfflineTypingIndicator(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "offline_typing")

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00BFA5))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.SmartToy, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .background(if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val delay = index * 200
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "offline_dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(Color(0xFF00E676).copy(alpha = alpha), CircleShape)
                    )
                }
            }
        }
    }
}

// ─── Quick Suggestion Chips ───

@Composable
private fun OfflineSuggestionChips(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "Quadratic formula",
        "Newton's laws",
        "Python basics",
        "English tenses",
        "Study tips",
        "Cell structure"
    )

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            "Quick Topics:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                suggestions.take(3).forEach { s ->
                SuggestionChip(
                    onClick = { onSuggestionClick(s) },
                    label = { Text(s, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    shape = RoundedCornerShape(20.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.drop(3).forEach { s ->
                SuggestionChip(
                    onClick = { onSuggestionClick(s) },
                    label = { Text(s, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                    shape = RoundedCornerShape(20.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

// ─── Input Bar ───

@Composable
private fun OfflineInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isTyping: Boolean,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .border(
                1.dp,
                if (isDark) Color(0xFF00E676).copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Offline indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF00E676).copy(alpha = 0.3f), Color(0xFF00BFA5).copy(alpha = 0.3f))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.WifiOff,
                contentDescription = "Offline",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(4.dp))

        // Text input
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text("Ask a study question...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
            },
            modifier = Modifier.weight(1f),
            singleLine = false,
            maxLines = 4,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = Color(0xFF00E676)
            )
        )

        // Send button
        AnimatedVisibility(
            visible = inputText.isNotBlank(),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isTyping,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF00E676))),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Utility ───

private fun formatOfflineTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}


