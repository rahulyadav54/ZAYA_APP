package com.zayaai.app

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel, paddingValues: PaddingValues) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // ─── Attachment State ───
    var pendingAttachmentUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAttachmentName by remember { mutableStateOf<String?>(null) }
    var pendingAttachmentType by remember { mutableStateOf<String?>(null) } // "image", "pdf", "text", etc.
    var showAttachmentOptions by remember { mutableStateOf(false) }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingAttachmentUri = uri
            pendingAttachmentName = FileProcessingUtils.getFileNameFromUri(context, uri)
            pendingAttachmentType = "image"
        }
    }

    // Document Picker (PDF, text, etc.)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = FileProcessingUtils.getMimeType(context, uri)
            pendingAttachmentUri = uri
            pendingAttachmentName = FileProcessingUtils.getFileNameFromUri(context, uri)
            pendingAttachmentType = FileProcessingUtils.getAttachmentType(mimeType)
        }
    }

    // TTS for reading AI responses aloud
    val ttsManager = remember { TextToSpeechManager(context) }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()

    // Speech recognition for voice input
    val speechManager = remember { SpeechRecognizerManager(context) }
    val isListening by speechManager.isListening.collectAsState()
    val rmsLevel by speechManager.rmsLevel.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechManager.startListening { text ->
                if (text.isNotBlank() && !text.startsWith("Error") && !text.startsWith("No speech") && text != "Listening...") {
                    viewModel.sendMessage(text)
                }
            }
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(viewModel.messages.size, viewModel.isTyping) {
        if (viewModel.messages.isNotEmpty()) {
            val targetIndex = viewModel.messages.size - 1 + if (viewModel.isTyping) 1 else 0
            try {
                listState.animateScrollToItem(targetIndex)
            } catch (_: Exception) { }
        }
    }

    // Cleanup TTS on dispose
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.destroy()
            speechManager.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding()
    ) {
        // ─── Chat Header ───
        ChatHeader(
            isSpeaking = isSpeaking,
            onStopSpeaking = { ttsManager.stop() },
            conversationTitle = viewModel.currentConversationTitle,
            onNewChat = {
                pendingAttachmentUri = null
                pendingAttachmentName = null
                pendingAttachmentType = null
                viewModel.startNewChat()
            }
        )

        // ─── Messages List ───
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Empty state
            if (viewModel.messages.isEmpty() && !viewModel.isTyping) {
                item { EmptyChatState() }
            }

            items(viewModel.messages, key = { it.timestamp }) { message ->
                ChatMessageBubble(
                    message = message,
                    onSpeakMessage = { text -> ttsManager.speak(text) }
                )
            }

            // Typing indicator
            if (viewModel.isTyping) {
                item { TypingIndicator() }
            }
        }

        // ─── Voice Listening Indicator ───
        AnimatedVisibility(
            visible = isListening,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            VoiceListeningBar(rmsLevel = rmsLevel)
        }

        // ─── Attachment Preview ───
        AnimatedVisibility(
            visible = pendingAttachmentUri != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            pendingAttachmentUri?.let { uri ->
                AttachmentPreviewBar(
                    uri = uri,
                    fileName = pendingAttachmentName ?: "Attachment",
                    attachmentType = pendingAttachmentType ?: "file",
                    onRemove = {
                        pendingAttachmentUri = null
                        pendingAttachmentName = null
                        pendingAttachmentType = null
                    }
                )
            }
        }

        // ─── Attachment Options Bottom Sheet ───
        if (showAttachmentOptions) {
            AttachmentOptionsSheet(
                onDismiss = { showAttachmentOptions = false },
                onPickImage = {
                    showAttachmentOptions = false
                    imagePickerLauncher.launch("image/*")
                },
                onPickDocument = {
                    showAttachmentOptions = false
                    documentPickerLauncher.launch(arrayOf(
                        "application/pdf",
                        "text/plain",
                        "text/csv",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/html",
                        "application/json"
                    ))
                },
                onTakePhoto = {
                    showAttachmentOptions = false
                    imagePickerLauncher.launch("image/*")
                }
            )
        }

        // ─── Input Bar ───
        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                val uri = pendingAttachmentUri
                if (uri != null) {
                    // Send with attachment
                    viewModel.sendMessageWithAttachment(inputText, uri, context)
                    inputText = ""
                    pendingAttachmentUri = null
                    pendingAttachmentName = null
                    pendingAttachmentType = null
                } else if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            },
            onMicClick = {
                if (isListening) {
                    speechManager.stopListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onAttachClick = { showAttachmentOptions = true },
            isListening = isListening,
            isTyping = viewModel.isTyping,
            hasAttachment = pendingAttachmentUri != null
        )
    }
}

// ─── Chat Header ───

@Composable
private fun ChatHeader(
    isSpeaking: Boolean,
    onStopSpeaking: () -> Unit,
    conversationTitle: String = "New Chat",
    onNewChat: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark)
                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background))
                else
                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AI Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Z", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "AI ZAYA",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                conversationTitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) NeonCyan else MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        if (isSpeaking) {
            IconButton(onClick = onStopSpeaking) {
                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Stop speaking", tint = NeonCyan)
            }
        }

        // New Chat button
        IconButton(onClick = onNewChat) {
            Icon(Icons.Rounded.AddComment, contentDescription = "New Chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Empty Chat State ───

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.3f), NeonCyan.copy(alpha = 0.3f))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Z", color = NeonCyan, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Hello! I'm ZAYA",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Ask me anything, upload an image or document 📎",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// --- Chat Message Bubble ---

@Composable
fun ChatMessageBubble(message: Message, onSpeakMessage: (String) -> Unit = {}) {
    val isUser = message.isUser
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI Avatar (left side)
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Z", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = if (isUser) 300.dp else 340.dp)
        ) {
            // Bubble
            Card(
                shape = if (isUser)
                    RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
                else
                    RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.then(
                    if (!isUser)
                        Modifier.border(
                            1.dp,
                            if (isDark) NeonCyan.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
                        )
                    else Modifier
                )
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            if (isUser) {
                                if (isDark)
                                    Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.6f), NeonCyan.copy(alpha = 0.4f)))
                                else
                                    Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.85f), NeonCyan.copy(alpha = 0.7f)))
                            } else {
                                if (isDark)
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, Color(0xFF252538)))
                                else
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.surface))
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (isUser) {
                        // Show attachment badge if present
                        if (message.attachmentName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = when (message.attachmentType) {
                                        "image" -> Icons.Rounded.Image
                                        "pdf" -> Icons.Rounded.PictureAsPdf
                                        else -> Icons.AutoMirrored.Rounded.InsertDriveFile
                                    },
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.attachmentName ?: "Attachment",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
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

            // Action row for AI messages
            if (!isUser) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Speak button
                    IconButton(
                        onClick = { onSpeakMessage(message.text) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Speak",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("message", message.text))
                            android.widget.Toast.makeText(context, "Copied!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = formatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // User Avatar (right side)
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isDark) NeonPurple.copy(alpha = 0.5f) else NeonPurple.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Typing Indicator ───

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // AI Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("Z", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val delay = index * 200
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    val yOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "y_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .offset(y = yOffset.dp)
                            .background(
                                NeonCyan.copy(alpha = alpha),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ─── Voice Listening Bar ───

@Composable
private fun VoiceListeningBar(rmsLevel: Float) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "listening_bar")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF1A0F2E) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Waveform bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(20) { i ->
                val barHeight = ((rmsLevel * 30f) + (5f * kotlin.math.sin((i * 0.5f) + (rmsLevel * 10f))).toFloat())
                    .coerceIn(4f, 32f)
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(barHeight.dp)
                        .background(
                            if (isDark) NeonCyan.copy(alpha = pulseAlpha * 0.8f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.8f),
                            RoundedCornerShape(50)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text("Listening...", color = if (isDark) NeonCyan else MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ─── Chat Input Bar ───

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onAttachClick: () -> Unit,
    isListening: Boolean,
    isTyping: Boolean,
    hasAttachment: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .border(
                1.dp,
                if (isDark) NeonCyan.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Attachment button
        IconButton(
            onClick = onAttachClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (hasAttachment)
                        Brush.linearGradient(listOf(NeonPurple, NeonCyan))
                    else
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Rounded.AttachFile,
                contentDescription = "Attach file",
                tint = if (hasAttachment) Color.White
                       else if (isDark) NeonCyan.copy(alpha = 0.7f)
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Microphone button
        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isListening)
                        Brush.linearGradient(listOf(Color(0xFFFF5252), Color(0xFFFF1744)))
                    else if (isDark)
                        Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.3f), NeonCyan.copy(alpha = 0.3f)))
                    else
                        Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.7f), NeonCyan.copy(alpha = 0.6f))),
                    CircleShape
                )
        ) {
            Icon(
                if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = if (isListening) "Stop" else "Speak",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Text Input
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    if (isListening) "Listening..." else "Ask ZAYA anything...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
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
                cursorColor = NeonCyan
            )
        )

        // Send button
        AnimatedVisibility(
            visible = inputText.isNotBlank() || hasAttachment,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            IconButton(
                onClick = onSend,
                enabled = (inputText.isNotBlank() || hasAttachment) && !isTyping,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
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

// ─── Attachment Preview Bar ───

@Composable
private fun AttachmentPreviewBar(
    uri: Uri,
    fileName: String,
    attachmentType: String,
    onRemove: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(
                if (isDark) Color(0xFF1E1E30) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isDark) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or icon
        if (attachmentType == "image") {
            val bitmap = remember(uri) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bmp = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    bmp
                } catch (_: Exception) { null }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image preview",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                AttachmentIcon(attachmentType, isDark)
            }
        } else {
            AttachmentIcon(attachmentType, isDark)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when (attachmentType) {
                    "image" -> "📷 Image ready to send"
                    "pdf" -> "📄 PDF document"
                    "text" -> "📝 Text file"
                    else -> "📎 File attached"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) NeonCyan.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary
            )
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Remove attachment",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AttachmentIcon(attachmentType: String, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                if (isDark) Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.3f), NeonCyan.copy(alpha = 0.3f)))
                else Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.15f), NeonCyan.copy(alpha = 0.15f))),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (attachmentType) {
                "image" -> Icons.Rounded.Image
                "pdf" -> Icons.Rounded.PictureAsPdf
                "text" -> Icons.Rounded.Description
                else -> Icons.AutoMirrored.Rounded.InsertDriveFile
            },
            contentDescription = null,
            tint = if (isDark) NeonCyan else NeonPurple,
            modifier = Modifier.size(26.dp)
        )
    }
}

// ─── Attachment Options Sheet ───

@Composable
private fun AttachmentOptionsSheet(
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
    onTakePhoto: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Attach File",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Image option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPickImage() },
                    color = if (isDark) Color(0xFF1E1E30) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF6E72FC), Color(0xFF9D50BB))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Image", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("JPG, PNG, WEBP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Document option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPickDocument() },
                    color = if (isDark) Color(0xFF1E1E30) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFFFF5252), Color(0xFFFF9800))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Document", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("PDF, TXT, DOC, CSV, JSON", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// ─── Utility ───

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

