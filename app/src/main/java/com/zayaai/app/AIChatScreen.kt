package com.zayaai.app

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
    var messageToReport by remember { mutableStateOf<Message?>(null) }

    if (messageToReport != null) {
        val msg = messageToReport!!
        ReportContentDialog(
            contentSnippet = msg.text,
            contentType = "AI Message",
            onDismiss = { messageToReport = null },
            onSubmitReport = { reason, details ->
                viewModel.reportMessage(msg, reason, details)
                android.widget.Toast.makeText(context, "Thank you. Report submitted for safety review.", android.widget.Toast.LENGTH_SHORT).show()
                messageToReport = null
            }
        )
    }

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
                item {
                    EmptyChatState(
                        onSuggestionClick = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                }
            }

            items(viewModel.messages, key = { it.timestamp }) { message ->
                ChatMessageBubble(
                    message = message,
                    isSpeakingThisMessage = isSpeaking,
                    onSpeakMessage = { text -> ttsManager.speak(text) },
                    onStopSpeaking = { ttsManager.stop() },
                    onRegenerate = { viewModel.regenerateLastResponse() },
                    onShareMessage = { text ->
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share AI ZAYA Response"))
                    },
                    onReportMessage = { msg -> messageToReport = msg }
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
private fun EmptyChatState(onSuggestionClick: (String) -> Unit = {}) {
    val isDark = LocalIsDarkTheme.current
    val suggestions = listOf(
        "💡 Help me study for exams",
        "💻 Write a Python script",
        "✍️ Draft a professional email",
        "🔬 Explain quantum physics simply"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF38BDF8))
                    )
                )
                .padding(2.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF141527) else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "Hello! I'm ZAYA 👋",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = if (isDark) Color.White else Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your Futuristic AI Assistant for study, code, and productivity.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "TRY ASKING",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                fontSize = 11.sp
            ),
            color = Color(0xFF7C3AED)
        )

        Spacer(modifier = Modifier.height(10.dp))

        suggestions.forEach { suggestion ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF141527) else Color(0xFFFFFFFF),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF232544) else Color(0xFFE2E8F0)
                ),
                shadowElevation = if (isDark) 0.dp else 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSuggestionClick(suggestion.substringAfter(" ")) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// --- Chat Message Bubble ---

@Composable
fun ChatMessageBubble(
    message: Message,
    isSpeakingThisMessage: Boolean = false,
    onSpeakMessage: (String) -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    onRegenerate: () -> Unit = {},
    onShareMessage: (String) -> Unit = {},
    onReportMessage: ((Message) -> Unit)? = null
) {
    val isUser = message.isUser
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var feedback by remember { mutableStateOf<Boolean?>(null) }

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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF38BDF8)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "AI ZAYA",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = if (isUser) {
                Modifier.widthIn(max = 300.dp)
            } else {
                Modifier.weight(1f).padding(end = 4.dp)
            }
        ) {
            if (isUser) {
                // User Bubble
                Card(
                    shape = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1))))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        if (message.attachmentName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
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
                                    text = message.attachmentName,
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
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 4.dp)
                )
            } else {
                // Professional ChatGPT-Quality AI Response Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF141527) else Color(0xFFFFFFFF),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF232544) else Color(0xFFE2E8F0)
                    ),
                    shadowElevation = if (isDark) 0.dp else 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // AI Model and Status Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI ZAYA",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1)
                                )
                                Text(
                                    text = " • Nemotron 3 Ultra",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            }

                            Text(
                                text = formatTimestamp(message.timestamp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }

                        // Rich Markdown Body
                        MarkdownText(
                            text = message.text,
                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            color = if (isDark) Color(0xFF20233B) else Color(0xFFF1F5F9),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Compact Action Toolbar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // 1. Copy
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("AI ZAYA Response", message.text))
                                        android.widget.Toast.makeText(context, "Response copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "Copy Response",
                                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // 2. Read Aloud / TTS
                                IconButton(
                                    onClick = {
                                        if (isSpeakingThisMessage) {
                                            onStopSpeaking()
                                        } else {
                                            onSpeakMessage(message.text)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeakingThisMessage) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = if (isSpeakingThisMessage) "Stop Speaking" else "Read Aloud",
                                        tint = if (isSpeakingThisMessage) Color(0xFF10B981) else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                // 3. Share
                                IconButton(
                                    onClick = { onShareMessage(message.text) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Share Response",
                                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // 4. Regenerate
                                IconButton(
                                    onClick = { onRegenerate() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Regenerate Response",
                                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // 5. Report Inappropriate Content (Google Play AI Policy Mandate)
                                if (onReportMessage != null) {
                                    IconButton(
                                        onClick = { onReportMessage(message) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Flag,
                                            contentDescription = "Report Inappropriate Response",
                                            tint = if (isDark) Color(0xFFF87171) else Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Like / Dislike feedback
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        feedback = if (feedback == true) null else true
                                        if (feedback == true) {
                                            android.widget.Toast.makeText(context, "Thanks for the feedback!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbUp,
                                        contentDescription = "Good response",
                                        tint = if (feedback == true) Color(0xFF10B981) else if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        feedback = if (feedback == false) null else false
                                        if (feedback == false) {
                                            android.widget.Toast.makeText(context, "Feedback noted. We will improve!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ThumbDown,
                                        contentDescription = "Bad response",
                                        tint = if (feedback == false) Color(0xFFEF4444) else if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
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
    val lastSendClickTime = remember { mutableStateOf(0L) }
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
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastSendClickTime.value > 750) {
                        lastSendClickTime.value = now
                        onSend()
                    }
                },
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

