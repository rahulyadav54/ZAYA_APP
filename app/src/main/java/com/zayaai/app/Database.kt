package com.zayaai.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import android.os.Environment
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import java.io.File

// Entities
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val front: String,
    val back: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val task: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "formulas")
data class FormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String,
    val name: String,
    val formula: String,
    val description: String
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String = "New Chat",
    val lastMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conversationId: String = "",
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val category: String = "General"
)

@Entity(tableName = "mind_map_nodes")
data class MindMapNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mapId: String,
    val text: String,
    val positionX: Float,
    val positionY: Float
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subject: String,
    val date: Long, // timestamp
    val startTime: String,
    val endTime: String
)

@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_articles")
data class StudyArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── AI Learning Feature Entities ───

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val title: String,
    val fileType: String, // pdf, image, text, manual
    val extractedText: String,
    val firestoreId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_questions")
data class GeneratedQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val materialId: Long,
    val question: String,
    val answer: String
)

@Entity(tableName = "ai_quiz_questions")
data class AIQuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val materialId: Long,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val questionType: String = "mcq", // mcq, true_false, short_answer
    val difficulty: String = "medium" // easy, medium, hard
)

@Entity(tableName = "ai_flashcards")
data class AIFlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val materialId: Long,
    val frontText: String,
    val backText: String
)

@Entity(tableName = "study_summaries")
data class StudySummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val materialId: Long,
    val summary: String
)

@Entity(tableName = "user_performance")
data class UserPerformanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val materialId: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val difficulty: String = "medium",
    val timestamp: Long = System.currentTimeMillis()
)

// DAOs
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY timestamp DESC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity)
    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    @Update
    suspend fun updateTask(task: TaskEntity)
    @Delete
    suspend fun deleteTask(task: TaskEntity)
}

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formulas")
    fun getAllFormulas(): Flow<List<FormulaEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaEntity)
    @Delete
    suspend fun deleteFormula(formula: FormulaEntity)
    @Query("DELETE FROM formulas")
    suspend fun deleteAllFormulas()
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions")
    fun getAllQuestions(): Flow<List<QuizQuestionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuizQuestionEntity)
}

@Dao
interface MindMapDao {
    @Query("SELECT * FROM mind_map_nodes WHERE mapId = :mapId")
    fun getNodesForMap(mapId: String): Flow<List<MindMapNodeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: MindMapNodeEntity)
    @Delete
    suspend fun deleteNode(node: MindMapNodeEntity)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans ORDER BY date ASC")
    fun getAllPlans(): Flow<List<StudyPlanEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlanEntity)
    @Delete
    suspend fun deletePlan(plan: StudyPlanEntity)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    @Query("UPDATE conversations SET title = :title, lastMessage = :lastMessage, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateConversation(id: String, title: String, lastMessage: String, updatedAt: Long)
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatHistoryEntity>>
    @Query("SELECT * FROM chat_history WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatHistoryEntity>>
    @Query("SELECT * FROM chat_history WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: String, limit: Int): List<ChatHistoryEntity>
    @Insert
    suspend fun insertChatMessage(chat: ChatHistoryEntity)
    @Query("DELETE FROM chat_history WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: String)
    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}

@Dao
interface AiCacheDao {
    @Query("SELECT * FROM ai_cache WHERE prompt = :prompt LIMIT 1")
    suspend fun getCachedResponse(prompt: String): AiCacheEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AiCacheEntity)
    @Query("DELETE FROM ai_cache WHERE timestamp < :expiry")
    suspend fun clearOldCache(expiry: Long)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
}

@Dao
interface StudyArticleDao {
    @Query("SELECT * FROM study_articles ORDER BY timestamp DESC")
    fun getAllArticles(): Flow<List<StudyArticleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: StudyArticleEntity)
    @Delete
    suspend fun deleteArticle(article: StudyArticleEntity)
}

// ─── AI Learning Feature DAOs ───

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_materials ORDER BY timestamp DESC")
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>
    @Query("SELECT * FROM study_materials WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMaterialsByUser(userId: String): Flow<List<StudyMaterialEntity>>
    @Query("SELECT * FROM study_materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: Long): StudyMaterialEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity): Long
    @Delete
    suspend fun deleteMaterial(material: StudyMaterialEntity)
    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Long)
}

@Dao
interface GeneratedQuestionDao {
    @Query("SELECT * FROM generated_questions WHERE materialId = :materialId")
    fun getQuestionsForMaterial(materialId: Long): Flow<List<GeneratedQuestionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: GeneratedQuestionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<GeneratedQuestionEntity>)
    @Query("DELETE FROM generated_questions WHERE materialId = :materialId")
    suspend fun deleteForMaterial(materialId: Long)
}

@Dao
interface AIQuizQuestionDao {
    @Query("SELECT * FROM ai_quiz_questions WHERE materialId = :materialId")
    fun getQuizForMaterial(materialId: Long): Flow<List<AIQuizQuestionEntity>>
    @Query("SELECT * FROM ai_quiz_questions WHERE materialId = :materialId AND difficulty = :difficulty")
    fun getQuizByDifficulty(materialId: Long, difficulty: String): Flow<List<AIQuizQuestionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<AIQuizQuestionEntity>)
    @Query("DELETE FROM ai_quiz_questions WHERE materialId = :materialId")
    suspend fun deleteForMaterial(materialId: Long)
}

@Dao
interface AIFlashcardDao {
    @Query("SELECT * FROM ai_flashcards WHERE materialId = :materialId")
    fun getFlashcardsForMaterial(materialId: Long): Flow<List<AIFlashcardEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<AIFlashcardEntity>)
    @Query("DELETE FROM ai_flashcards WHERE materialId = :materialId")
    suspend fun deleteForMaterial(materialId: Long)
}

@Dao
interface StudySummaryDao {
    @Query("SELECT * FROM study_summaries WHERE materialId = :materialId LIMIT 1")
    suspend fun getSummaryForMaterial(materialId: Long): StudySummaryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: StudySummaryEntity)
    @Query("DELETE FROM study_summaries WHERE materialId = :materialId")
    suspend fun deleteForMaterial(materialId: Long)
}

@Dao
interface UserPerformanceDao {
    @Query("SELECT * FROM user_performance WHERE userId = :userId AND materialId = :materialId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPerformance(userId: String, materialId: Long): UserPerformanceEntity?
    @Query("SELECT * FROM user_performance WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllPerformance(userId: String): Flow<List<UserPerformanceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: UserPerformanceEntity)
}

// Database
@Database(
    entities = [
        NoteEntity::class, 
        FlashcardEntity::class, 
        TaskEntity::class, 
        BookmarkEntity::class, 
        FormulaEntity::class,
        ConversationEntity::class,
        ChatHistoryEntity::class,
        QuizQuestionEntity::class,
        MindMapNodeEntity::class,
        StudyPlanEntity::class,
        AiCacheEntity::class,
        StudyArticleEntity::class,
        StudyMaterialEntity::class,
        GeneratedQuestionEntity::class,
        AIQuizQuestionEntity::class,
        AIFlashcardEntity::class,
        StudySummaryEntity::class,
        UserPerformanceEntity::class
    ], 
    version = 6, 
    exportSchema = false
)
abstract class AiZayaDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun taskDao(): TaskDao
    abstract fun formulaDao(): FormulaDao
    abstract fun quizDao(): QuizDao
    abstract fun mindMapDao(): MindMapDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun conversationDao(): ConversationDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun aiCacheDao(): AiCacheDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun studyArticleDao(): StudyArticleDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun generatedQuestionDao(): GeneratedQuestionDao
    abstract fun aiQuizQuestionDao(): AIQuizQuestionDao
    abstract fun aiFlashcardDao(): AIFlashcardDao
    abstract fun studySummaryDao(): StudySummaryDao
    abstract fun userPerformanceDao(): UserPerformanceDao

    companion object {
        @Volatile
        private var INSTANCE: AiZayaDatabase? = null
        fun getDatabase(context: Context): AiZayaDatabase {
            return INSTANCE ?: synchronized(this) {
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                val dbPath = if (hasPermission && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val externalDir = Environment.getExternalStorageDirectory()
                    val dbDir = File(externalDir, "AIZAYA")
                    if (!dbDir.exists()) dbDir.mkdirs()
                    File(dbDir, "aizaya_database.db").absolutePath
                } else {
                    "aizaya_database"
                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AiZayaDatabase::class.java,
                    dbPath
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

