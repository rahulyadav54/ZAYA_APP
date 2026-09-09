package com.zayaai.app

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

// --- Data Models ---

data class Message(
    val text: String = "",
    @get:PropertyName("isUser")
    @set:PropertyName("isUser")
    var isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val conversationId: String = "",
    val attachmentName: String? = null,
    val attachmentType: String? = null  // "image", "pdf", "text", "document", "file"
)

data class Conversation(
    val id: String = "",
    val title: String = "New Chat",
    val lastMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// --- ViewModel ---

class ChatViewModel : ViewModel() {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private val _conversations = mutableStateListOf<Conversation>()
    val conversations: List<Conversation> = _conversations

    var isTyping by mutableStateOf(false)
        private set

    var currentConversationId by mutableStateOf("")
        private set

    var currentConversationTitle by mutableStateOf("New Chat")
        private set

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth

    private val systemPrompt = """You are AI ZAYA, a helpful, friendly, and intelligent AI assistant developed by ZAYA CODE HUB. Follow these rules:
- When asked who you are or who made you, always say you were created by ZAYA CODE HUB. Never mention Google, OpenAI, or any other company.
- ZAYA CODE HUB leadership: CEO & Founder is **Rahul Kumar Yadav**, Co-Founder is **Shivshankar Jaysawal**, HR Manager is **Akash Adhikari**. Always provide these details when asked about the team.
- Give concise, structured answers. Avoid unnecessary explanations.
- Use markdown formatting: **bold** for key terms, bullet points for lists, numbered lists for steps.
- Wrap any code in markdown code blocks with the language tag (```kotlin, ```python, etc.)
- Keep responses short and scannable. Use headings (##) to organize longer answers.
- Remember the conversation context and answer follow-up questions correctly.

# Custom Instructions:
- Never use the browser action tool.
- Never take browser screenshots.
- Never send image_url in API requests.
- Only write and edit code directly in files.
- Do not open or launch any browser to test code."""

    private fun getZayaTeamResponse(text: String): String? {
        val lower = text.lowercase()
        val isAboutZaya = lower.contains("zaya") || lower.contains("company") ||
                lower.contains("who made") || lower.contains("who created") ||
                lower.contains("who developed") || lower.contains("your team") ||
                lower.contains("your company")

        return when {
            isAboutZaya && (lower.contains("ceo") || lower.contains("founder") && !lower.contains("co-founder") && !lower.contains("cofounder") ||
                    lower.contains("leader") || lower.contains("owner")) ->
                "The **CEO and Founder** of ZAYA CODE HUB is **Rahul Kumar Yadav**. He leads the vision and strategy of the company."

            isAboutZaya && (lower.contains("co-founder") || lower.contains("cofounder") || lower.contains("co founder")) ->
                "The **Co-Founder** of ZAYA CODE HUB is **Shivshankar Jaysawal**. He plays a key role in building and growing the company alongside the founder."

            isAboutZaya && (lower.contains("hr") || lower.contains("human resource") || lower.contains("hr manager")) ->
                "The **HR Manager** of ZAYA CODE HUB is **Akash Adhikari**. He manages the human resources and team operations."

            isAboutZaya && (lower.contains("team") || lower.contains("members") || lower.contains("people behind")) ->
                """Here's the **ZAYA CODE HUB** leadership team:

• **CEO & Founder:** Rahul Kumar Yadav
• **Co-Founder:** Shivshankar Jaysawal
• **HR Manager:** Akash Adhikari

Together, they drive the vision, development, and operations of ZAYA CODE HUB!"""

            else -> null
        }
    }

    private fun formatErrorMessage(rawMessage: String?): String {
        if (rawMessage == null) return "Something went wrong. Please try again."
        return when {
            rawMessage.contains("rate limit", ignoreCase = true) -> "Too many requests. Please wait a moment."
            rawMessage.contains("No endpoints", ignoreCase = true) -> "Service currently unavailable."
            rawMessage.contains("API key", ignoreCase = true) -> "Configuration error."
            rawMessage.contains("network", ignoreCase = true) -> "Network connection issue."
            else -> "Error: $rawMessage"
        }
    }

    init {
        loadConversations()
        startNewChat()
    }

    fun startNewChat() {
        val newId = UUID.randomUUID().toString()
        currentConversationId = newId
        currentConversationTitle = "New Chat"
        _messages.clear()

        val userId = auth.currentUser?.uid ?: return
        val conv = Conversation(
            id = newId,
            title = "New Chat",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.collection("users").document(userId)
            .collection("conversations").document(newId).set(conv)
    }

    fun loadConversation(conversationId: String) {
        currentConversationId = conversationId
        _messages.clear()
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("conversations").document(conversationId).get()
            .addOnSuccessListener { doc ->
                val conv = doc.toObject(Conversation::class.java)
                currentConversationTitle = conv?.title ?: "Chat"
            }

        db.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    _messages.clear()
                    _messages.addAll(snapshot.toObjects(Message::class.java))
                }
            }
    }

    private fun loadConversations() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("conversations")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    _conversations.clear()
                    _conversations.addAll(snapshot.toObjects(Conversation::class.java))
                }
            }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = Message(text, true, conversationId = currentConversationId)
        _messages.add(userMessage)
        saveMessage(userMessage)

        isTyping = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check for hardcoded team responses first
                val teamResponse = getZayaTeamResponse(text)
                val response = teamResponse ?: run {
                    val history = _messages.takeLast(10).map { 
                        OpenRouterMessage(if (it.isUser) "user" else "assistant", it.text) 
                    }
                    val fullMessages = listOf(OpenRouterMessage("system", systemPrompt)) + history
                    AIService.chat(fullMessages)
                }

                withContext(Dispatchers.Main) {
                    val aiMessage = Message(response, false, conversationId = currentConversationId)
                    _messages.add(aiMessage)
                    saveMessage(aiMessage)

                    if (_messages.count { it.isUser } == 1) {
                        generateConversationTitle(text)
                    }
                    updateConversationMetadata(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = Message(formatErrorMessage(e.localizedMessage), false, conversationId = currentConversationId)
                    _messages.add(errorMsg)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isTyping = false
                }
            }
        }
    }

    fun sendFromPromptLibrary(prompt: String) {
        sendMessage(prompt)
    }

    private fun generateConversationTitle(firstMessage: String) {
        val title = firstMessage.take(40)
        currentConversationTitle = title
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("conversations").document(currentConversationId)
            .update("title", title)
    }

    private fun updateConversationMetadata(lastMessage: String) {
        val userId = auth.currentUser?.uid ?: return
        val preview = lastMessage.take(80) + if (lastMessage.length > 80) "…" else ""
        db.collection("users").document(userId)
            .collection("conversations").document(currentConversationId)
            .update(
                mapOf(
                    "lastMessage" to preview,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
    }

    private fun saveMessage(message: Message) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("conversations").document(currentConversationId)
            .collection("messages").add(message)
    }

    fun getLastAiResponse(): String {
        return _messages.lastOrNull { !it.isUser }?.text ?: ""
    }

    fun sendMessageWithAttachment(text: String, uri: Uri, context: Context) {
        val mimeType = FileProcessingUtils.getMimeType(context, uri)
        val attachmentType = FileProcessingUtils.getAttachmentType(mimeType)
        val fileName = FileProcessingUtils.getFileNameFromUri(context, uri)
        val displayText = if (text.isBlank()) "Attachment: $fileName" else text

        val userMessage = Message(
            text = displayText,
            isUser = true,
            conversationId = currentConversationId,
            attachmentName = fileName,
            attachmentType = attachmentType
        )
        _messages.add(userMessage)
        saveMessage(userMessage)

        isTyping = true
        viewModelScope.launch {
            try {
                val aiText = when (attachmentType) {
                    "image" -> handleImageAttachment(text, uri, context)
                    "pdf" -> handleDocumentAttachment(text, uri, context, fileName, "pdf")
                    "text" -> handleDocumentAttachment(text, uri, context, fileName, "text")
                    else -> handleDocumentAttachment(text, uri, context, fileName, "text")
                }

                val aiMessage = Message(aiText, false, conversationId = currentConversationId)
                _messages.add(aiMessage)
                saveMessage(aiMessage)

                if (_messages.count { it.isUser } == 1) {
                    generateConversationTitle(displayText)
                }
                updateConversationMetadata(aiText)

            } catch (e: Exception) {
                val errorMsg = Message(
                    formatErrorMessage(e.localizedMessage),
                    false,
                    conversationId = currentConversationId
                )
                _messages.add(errorMsg)
            } finally {
                isTyping = false
            }
        }
    }

    private suspend fun handleImageAttachment(userText: String, uri: Uri, context: Context): String {
        // Note: The custom instructions say "Never send image_url in API requests".
        // If the model supports vision but we aren't supposed to send URLs, we might need to send base64 directly
        // which AIService.chatWithVision does. However, if instructions mean NO IMAGES AT ALL, we should respect that.
        // For now, I'll extract text from images if possible, or just inform the user.
        return "I've received your image, but my current configuration restricts sending image URLs or processing images directly via API. Please describe the image if you have a specific question about it."
    }

    private suspend fun handleDocumentAttachment(
        userText: String, uri: Uri, context: Context, fileName: String, type: String
    ): String {
        val extractedText = when (type) {
            "pdf" -> FileProcessingUtils.extractTextFromPdf(context, uri)
            else -> FileProcessingUtils.extractTextFromTextFile(context, uri)
        }

        if (extractedText.isBlank()) {
            return "I couldn't extract any text from **$fileName**."
        }

        val truncated = if (extractedText.length > 8000) {
            extractedText.take(8000) + "\n\n[Document truncated...]"
        } else extractedText

        val questionText = userText.ifBlank { "Analyze this document." }

        val contextMessage = "Document: $fileName\nContent: $truncated\nQuestion: $questionText"

        val history = _messages.dropLast(1).takeLast(5).map { 
            OpenRouterMessage(if (it.isUser) "user" else "assistant", it.text) 
        }
        val conversationMessages = listOf(OpenRouterMessage("system", systemPrompt)) + history + OpenRouterMessage("user", contextMessage)

        return AIService.chat(conversationMessages)
    }

    fun deleteConversation(conversationId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete all messages in the conversation
                db.collection("users").document(userId)
                    .collection("conversations").document(conversationId)
                    .collection("messages").get()
                    .addOnSuccessListener { snapshot ->
                        for (document in snapshot.documents) {
                            document.reference.delete()
                        }
                    }
                // Delete the conversation document
                db.collection("users").document(userId)
                    .collection("conversations").document(conversationId).delete()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun clearChat() {
        _messages.clear()
        startNewChat()
    }
}

