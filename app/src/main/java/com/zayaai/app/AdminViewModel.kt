package com.zayaai.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // The authorized admin email as set in Firebase Authentication
    private val ADMIN_EMAIL = "zayacodehub@gmail.com"

    var isAdmin by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var operationMessage by mutableStateOf<String?>(null)
        private set

    fun clearMessage() {
        operationMessage = null
    }

    // Management Lists
    val quizzes = mutableStateListOf<FirestoreQuiz>()
    val formulas = mutableStateListOf<FirestoreFormula>()
    val announcements = mutableStateListOf<FirestoreAnnouncement>()
    val articles = mutableStateListOf<FirestoreArticle>()
    val allStudyMaterials = mutableStateListOf<FirestoreStudyMaterial>()
    val allPerformance = mutableStateListOf<FirestoreUserPerformance>()

    suspend fun checkAdminRole(): Boolean {
        val user = auth.currentUser ?: return false
        
        // Check if the email matches the authorized admin email
        val isAuthorizedEmail = user.email == ADMIN_EMAIL

        return if (isAuthorizedEmail) {
                // Also verify in Firestore for extra security if needed,
                // but the requirement says "set in authentication"
            try {
                val doc = db.collection("users").document(user.uid).get().await()
                val role = doc.getString("role")
                isAdmin = role == "admin" || isAuthorizedEmail
                isAdmin
            } catch (e: Exception) {
                // If firestore check fails, we still allow if email matches
                isAdmin = isAuthorizedEmail
                isAdmin
            }
        } else {
            isAdmin = false
            false
        }
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("quizzes").get().await()
                quizzes.clear()
                quizzes.addAll(snapshot.toObjects(FirestoreQuiz::class.java))
            } catch (e: Exception) {}
        }
    }

    fun saveQuiz(quiz: FirestoreQuiz) {
        viewModelScope.launch {
            isLoading = true
            try {
                val ref = if (quiz.id.isEmpty()) db.collection("quizzes").document() else db.collection("quizzes").document(quiz.id)
                val finalQuiz = quiz.copy(id = ref.id) 
                ref.set(finalQuiz).await()
                loadQuizzes()
                operationMessage = "Quiz saved successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to save quiz: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteQuiz(id: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                db.collection("quizzes").document(id).delete().await()
                loadQuizzes()
                operationMessage = "Quiz deleted successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to delete quiz: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Formulas
    fun loadFormulas() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("formulas").get().await()
                formulas.clear()
                formulas.addAll(snapshot.toObjects(FirestoreFormula::class.java))
            } catch (e: Exception) {}
        }
    }

    fun saveFormula(formula: FirestoreFormula) {
        viewModelScope.launch {
            isLoading = true
            try {
                val ref = if (formula.id.isEmpty()) db.collection("formulas").document() else db.collection("formulas").document(formula.id)
                val finalFormula = formula.copy(id = ref.id) 
                ref.set(finalFormula).await()
                loadFormulas()
                operationMessage = "Formula saved successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to save formula: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteFormula(id: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                db.collection("formulas").document(id).delete().await()
                loadFormulas()
                operationMessage = "Formula deleted successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to delete formula: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Announcements
    fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("announcements").get().await()
                announcements.clear()
                announcements.addAll(snapshot.toObjects(FirestoreAnnouncement::class.java))
            } catch (e: Exception) {}
        }
    }

    fun saveAnnouncement(ann: FirestoreAnnouncement) {
        viewModelScope.launch {
            isLoading = true
            try {
                val ref = if (ann.id.isEmpty()) db.collection("announcements").document() else db.collection("announcements").document(ann.id)
                val finalAnn = ann.copy(id = ref.id) 
                ref.set(finalAnn).await()
                loadAnnouncements()
                operationMessage = "Announcement saved successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to save announcement: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                db.collection("announcements").document(id).delete().await()
                loadAnnouncements()
                operationMessage = "Announcement deleted successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to delete announcement: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Articles
    fun loadArticles() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("articles").get().await()
                articles.clear()
                articles.addAll(snapshot.toObjects(FirestoreArticle::class.java))
            } catch (e: Exception) {}
        }
    }

    fun saveArticle(art: FirestoreArticle) {
        viewModelScope.launch {
            isLoading = true
            try {
                val ref = if (art.id.isEmpty()) db.collection("articles").document() else db.collection("articles").document(art.id)
                val finalArt = art.copy(id = ref.id)
                ref.set(finalArt).await()
                loadArticles()
                operationMessage = "Article saved successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to save article: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteArticle(id: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                db.collection("articles").document(id).delete().await()
                loadArticles()
                operationMessage = "Article deleted successfully!"
            } catch (e: Exception) {
                operationMessage = "Failed to delete article: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- Study Materials Admin ---

    fun loadAllStudyMaterials() {
        viewModelScope.launch {
            isLoading = true
            try {
                val snapshot = db.collection("study_materials").get().await()
                allStudyMaterials.clear()
                allStudyMaterials.addAll(snapshot.toObjects(FirestoreStudyMaterial::class.java))
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteStudyMaterial(materialId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Delete the material
                val materialDocs = db.collection("study_materials").whereEqualTo("id", materialId).get().await()
                materialDocs.documents.forEach { it.reference.delete().await() }

                // Delete associated generated questions
                val questionDocs = db.collection("generated_questions").whereEqualTo("materialId", materialId).get().await()
                questionDocs.documents.forEach { it.reference.delete().await() }

                // Delete associated quiz questions
                val quizDocs = db.collection("ai_quiz_questions").whereEqualTo("materialId", materialId).get().await()
                quizDocs.documents.forEach { it.reference.delete().await() }

                // Delete associated flashcards
                val flashcardDocs = db.collection("ai_flashcards").whereEqualTo("materialId", materialId).get().await()
                flashcardDocs.documents.forEach { it.reference.delete().await() }

                // Delete summaries
                val summaryDocs = db.collection("study_summaries").whereEqualTo("materialId", materialId).get().await()
                summaryDocs.documents.forEach { it.reference.delete().await() }

                loadAllStudyMaterials()
                operationMessage = "Material and all generated content deleted!"
            } catch (e: Exception) {
                operationMessage = "Failed to delete material: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadAllUserPerformance() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("user_performance").get().await()
                allPerformance.clear()
                allPerformance.addAll(snapshot.toObjects(FirestoreUserPerformance::class.java))
            } catch (_: Exception) {}
        }
    }
}

