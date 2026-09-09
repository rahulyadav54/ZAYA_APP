package com.zayaai.app

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for students to READ content managed by admin.
 * Fetches quizzes, formulas, announcements, and articles from Firestore.
 */
class StudentContentViewModel : ViewModel() {
    private val db = Firebase.firestore

    val announcements = mutableStateListOf<FirestoreAnnouncement>()
    val articles = mutableStateListOf<FirestoreArticle>()
    val quizzes = mutableStateListOf<FirestoreQuiz>()
    val formulas = mutableStateListOf<FirestoreFormula>()

    var isLoading by mutableStateOf(false)
        private set
    
    var hasErrorAnnouncements by mutableStateOf(false)
        private set
    var hasErrorArticles by mutableStateOf(false)
        private set
    var hasErrorQuizzes by mutableStateOf(false)
        private set
    var hasErrorFormulas by mutableStateOf(false)
        private set
    
    var errorMessageAnnouncements by mutableStateOf("")
        private set
    var errorMessageArticles by mutableStateOf("")
        private set
    var errorMessageQuizzes by mutableStateOf("")
        private set
    var errorMessageFormulas by mutableStateOf("")
        private set

    init {
        loadAllContent()
    }

    fun loadAllContent() {
        loadAnnouncements()
        loadArticles()
        loadQuizzes()
        loadFormulas()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                hasErrorAnnouncements = false
                val snapshot = db.collection("announcements").get().await()
                announcements.clear()
                announcements.addAll(snapshot.toObjects(FirestoreAnnouncement::class.java))
            } catch (e: Exception) {
                Log.e("StudentContent", "Failed to load announcements", e)
                hasErrorAnnouncements = true
                errorMessageAnnouncements = "Could not load announcements: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun loadArticles() {
        viewModelScope.launch {
            try {
                hasErrorArticles = false
                val snapshot = db.collection("articles").get().await()
                articles.clear()
                articles.addAll(snapshot.toObjects(FirestoreArticle::class.java))
            } catch (e: Exception) {
                Log.e("StudentContent", "Failed to load articles", e)
                hasErrorArticles = true
                errorMessageArticles = "Could not load articles: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            try {
                hasErrorQuizzes = false
                val snapshot = db.collection("quizzes").get().await()
                quizzes.clear()
                quizzes.addAll(snapshot.toObjects(FirestoreQuiz::class.java))
            } catch (e: Exception) {
                Log.e("StudentContent", "Failed to load quizzes", e)
                hasErrorQuizzes = true
                errorMessageQuizzes = "Could not load quizzes: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun loadFormulas() {
        viewModelScope.launch {
            try {
                hasErrorFormulas = false
                val snapshot = db.collection("formulas").get().await()
                formulas.clear()
                formulas.addAll(snapshot.toObjects(FirestoreFormula::class.java))
            } catch (e: Exception) {
                Log.e("StudentContent", "Failed to load formulas", e)
                hasErrorFormulas = true
                errorMessageFormulas = "Could not load formulas: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
}


