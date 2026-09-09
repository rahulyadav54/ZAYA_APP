package com.zayaai.app

import kotlinx.coroutines.flow.Flow

class AiZayaRepository(private val db: AiZayaDatabase) {

    // --- AI OPERATIONS WITH LOCAL CACHING (via OpenRouter) ---

    suspend fun getChatResponse(message: String): String {
        val cacheKey = "chat_$message"
        val cached = db.aiCacheDao().getCachedResponse(cacheKey)
        if (cached != null) return cached.response

        return try {
            val result = AIService.generate(message, "You are AI ZAYA, a helpful AI assistant developed by ZAYA CODE HUB. When asked who you are, always say you were created by ZAYA CODE HUB. Never mention Google, OpenAI, or any other company. Give concise, structured answers using markdown formatting: **bold** for key terms, bullet points for lists, code blocks with language tags.")
            db.aiCacheDao().insertCache(AiCacheEntity(cacheKey, result))
            result
        } catch (e: Exception) {
            throw Exception("AI is unavailable. Please check your internet connection.")
        }
    }

    /**
     * Context-aware chat: sends previous messages along with new one.
     */
    suspend fun getChatResponseWithContext(
        conversationId: String,
        newMessage: String,
        contextLimit: Int = 15
    ): String {
        val systemPrompt = "You are AI ZAYA, a helpful, friendly AI assistant developed by ZAYA CODE HUB. When asked who you are, always say you were created by ZAYA CODE HUB. Never mention Google, OpenAI, or any other company. Give concise, structured answers using markdown: **bold** for key terms, bullet points for lists, code blocks with language tags. Remember conversation context."

        // Fetch recent messages from this conversation for context
        val recentMessages = db.chatHistoryDao().getRecentMessages(conversationId, contextLimit)
            .sortedBy { it.timestamp } // ensure chronological order

        val conversationMessages = buildList {
            add(OpenRouterMessage("system", systemPrompt))
            recentMessages.forEach { msg ->
                add(OpenRouterMessage(if (msg.isUser) "user" else "assistant", msg.message))
            }
            add(OpenRouterMessage("user", newMessage))
        }

        return try {
            AIService.chat(conversationMessages)
        } catch (e: Exception) {
            throw Exception("AI is unavailable. Please check your internet connection.")
        }
    }

    suspend fun getTextGeneration(prompt: String): String {
        val cacheKey = "text_$prompt"
        val cached = db.aiCacheDao().getCachedResponse(cacheKey)
        if (cached != null) return cached.response

        return try {
            val result = AIService.generate(prompt, "Act as a professional writer.")
            db.aiCacheDao().insertCache(AiCacheEntity(cacheKey, result))
            result
        } catch (e: Exception) {
            throw Exception("Text generation failed.")
        }
    }

    suspend fun getCodeGeneration(prompt: String): String {
        val cacheKey = "code_$prompt"
        val cached = db.aiCacheDao().getCachedResponse(cacheKey)
        if (cached != null) return cached.response

        return try {
            val result = AIService.generate("Generate code for: $prompt. Always wrap code in markdown code blocks with the language tag. Keep explanations minimal.", "You are an expert software engineer. Output clean, production-ready code using markdown code blocks with language tags.")
            db.aiCacheDao().insertCache(AiCacheEntity(cacheKey, result))
            result
        } catch (e: Exception) {
            throw Exception("Code generation failed.")
        }
    }

    suspend fun getQuizGeneration(topic: String): String {
        return try {
            AIService.generate("Create a 5-question MCQ quiz about: $topic.", "You are a quiz creator.")
        } catch (e: Exception) {
            throw Exception("Quiz generation failed.")
        }
    }

    // --- DATABASE OPERATIONS ---

    // Notes
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    suspend fun insertNote(note: NoteEntity) = db.noteDao().insertNote(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)

    // Flashcards
    val allFlashcards: Flow<List<FlashcardEntity>> = db.flashcardDao().getAllFlashcards()
    suspend fun insertFlashcard(flashcard: FlashcardEntity) = db.flashcardDao().insertFlashcard(flashcard)
    suspend fun deleteFlashcard(flashcard: FlashcardEntity) = db.flashcardDao().deleteFlashcard(flashcard)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    suspend fun insertTask(task: TaskEntity) = db.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().deleteTask(task)

    // Formulas
    val allFormulas: Flow<List<FormulaEntity>> = db.formulaDao().getAllFormulas()
    suspend fun insertFormula(formula: FormulaEntity) = db.formulaDao().insertFormula(formula)
    suspend fun deleteFormula(formula: FormulaEntity) = db.formulaDao().deleteFormula(formula)
    suspend fun deleteAllFormulas() = db.formulaDao().deleteAllFormulas()

    // Quiz Questions
    val allQuizQuestions: Flow<List<QuizQuestionEntity>> = db.quizDao().getAllQuestions()
    suspend fun insertQuizQuestion(question: QuizQuestionEntity) = db.quizDao().insertQuestion(question)

    // Mind Map Nodes
    fun getMindMapNodes(mapId: String): Flow<List<MindMapNodeEntity>> = db.mindMapDao().getNodesForMap(mapId)
    suspend fun insertMindMapNode(node: MindMapNodeEntity) = db.mindMapDao().insertNode(node)
    suspend fun deleteMindMapNode(node: MindMapNodeEntity) = db.mindMapDao().deleteNode(node)

    // Study Plans
    val allStudyPlans: Flow<List<StudyPlanEntity>> = db.studyPlanDao().getAllPlans()
    suspend fun insertStudyPlan(plan: StudyPlanEntity) = db.studyPlanDao().insertPlan(plan)
    suspend fun deleteStudyPlan(plan: StudyPlanEntity) = db.studyPlanDao().deletePlan(plan)

    // Chat History
    val chatHistory: Flow<List<ChatHistoryEntity>> = db.chatHistoryDao().getChatHistory()
    suspend fun insertChatMessage(chat: ChatHistoryEntity) = db.chatHistoryDao().insertChatMessage(chat)
    suspend fun clearChatHistory() = db.chatHistoryDao().clearHistory()
    fun getMessagesForConversation(conversationId: String) = db.chatHistoryDao().getMessagesForConversation(conversationId)
    suspend fun getRecentMessages(conversationId: String, limit: Int) = db.chatHistoryDao().getRecentMessages(conversationId, limit)
    suspend fun clearConversation(conversationId: String) = db.chatHistoryDao().clearConversation(conversationId)

    // Conversations
    val allConversations: Flow<List<ConversationEntity>> = db.conversationDao().getAllConversations()
    suspend fun insertConversation(conversation: ConversationEntity) = db.conversationDao().insertConversation(conversation)
    suspend fun updateConversation(id: String, title: String, lastMessage: String, updatedAt: Long) =
        db.conversationDao().updateConversation(id, title, lastMessage, updatedAt)
    suspend fun deleteConversation(id: String) {
        db.conversationDao().deleteConversation(id)
        db.chatHistoryDao().clearConversation(id)
    }
    suspend fun getConversationById(id: String) = db.conversationDao().getConversationById(id)

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = db.bookmarkDao().getAllBookmarks()
    suspend fun insertBookmark(bookmark: BookmarkEntity) = db.bookmarkDao().insertBookmark(bookmark)
    suspend fun deleteBookmark(bookmark: BookmarkEntity) = db.bookmarkDao().deleteBookmark(bookmark)

    // Study Articles
    val allArticles: Flow<List<StudyArticleEntity>> = db.studyArticleDao().getAllArticles()
    suspend fun insertArticle(article: StudyArticleEntity) = db.studyArticleDao().insertArticle(article)
    suspend fun deleteArticle(article: StudyArticleEntity) = db.studyArticleDao().deleteArticle(article)
}

