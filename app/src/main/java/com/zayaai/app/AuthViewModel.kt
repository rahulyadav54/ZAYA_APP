package com.zayaai.app

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set

    var userRole by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var firestoreUserEmail by mutableStateOf<String?>(null)
        private set

    var firestoreUserName by mutableStateOf<String?>(null)
        private set

    var sessionChecked by mutableStateOf(false)
        private set

    /** Display name: uses Firebase displayName for admin, saved name for students */
    val displayName: String
        get() {
            // First try Firebase displayName (usually set for admins/Google users)
            val fbName = currentUser?.displayName?.takeIf { it.isNotBlank() }
            if (!fbName.isNullOrBlank()) return fbName
            
            // Then try Firestore saved name (for students)
            val fsName = firestoreUserName?.takeIf { it.isNotBlank() }
            if (!fsName.isNullOrBlank()) return fsName
            
            // If we have an email, extract the name part (before @)
            val emailPart = (currentUser?.email ?: firestoreUserEmail)?.takeIf { it.isNotBlank() }
            if (!emailPart.isNullOrBlank()) {
                val namePart = emailPart.substringBefore("@")
                // Capitalize first letter of each word
                return namePart.split(".").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
            
            return "User"
        }

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                // --- 1. Try Firebase Auth first (admin / Google users) ---
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    currentUser = firebaseUser
                    val role = withTimeoutOrNull(5000L) {
                        withContext(Dispatchers.IO) {
                            authRepository.getUserRole(firebaseUser.uid)
                        }
                    }
                    if (role != null) {
                        userRole = role
                        val name = firebaseUser.displayName
                            ?: withTimeoutOrNull(3000L) {
                                withContext(Dispatchers.IO) {
                                    authRepository.getUserNameByUid(firebaseUser.uid)
                                }
                            }
                            ?: "Admin"
                        firestoreUserName = name
                        dataStoreManager.saveSession(
                            email = firebaseUser.email ?: "",
                            role = role,
                            name = name
                        )
                        sessionChecked = true
                        return@launch
                    } else {
                        Log.w("AuthViewModel", "Role fetch failed for Firebase user, clearing")
                        try { authRepository.signOut() } catch (_: Exception) {}
                        currentUser = null
                    }
                }

                // --- 2. No Firebase user - check saved Firestore session (students) ---
                val savedSession = withContext(Dispatchers.IO) {
                    dataStoreManager.getSavedSession()
                }
                if (savedSession != null) {
                    firestoreUserEmail = savedSession.email
                    firestoreUserName = savedSession.name.ifBlank { null }
                    userRole = savedSession.role
                    currentUser = null
                    Log.d("AuthViewModel", "Restored session for ${savedSession.name} (${savedSession.email})")
                    sessionChecked = true
                    return@launch
                }

                // --- 3. No session at all - show login ---
                currentUser = null
                userRole = null
                firestoreUserEmail = null
                firestoreUserName = null

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Session restore failed", e)
                try { authRepository.signOut() } catch (_: Exception) {}
                currentUser = null
                userRole = null
                firestoreUserEmail = null
                firestoreUserName = null
            } finally {
                sessionChecked = true
            }
        }
    }

    fun login(email: String, pass: String, onResult: (Result<String>) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            val result = authRepository.login(email, pass)
            if (result.isSuccess) {
                val loginResult = result.getOrNull()!!
                userRole = loginResult.role
                val firebaseUser = authRepository.getCurrentUser()
                currentUser = firebaseUser
                firestoreUserName = loginResult.name
                firestoreUserEmail = null // All users now have Firebase Auth
                // Save session with name for persistence
                dataStoreManager.saveSession(
                    email = email,
                    role = loginResult.role,
                    name = loginResult.name
                )
                onResult(Result.success(loginResult.role))
            } else {
                firestoreUserEmail = null
                firestoreUserName = null
                onResult(Result.failure(result.exceptionOrNull() ?: Exception("Login failed")))
            }
            isLoading = false
            sessionChecked = true
        }
    }

    fun register(name: String, email: String, pass: String, onResult: (Result<Unit>) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            val result = authRepository.register(name, email, pass)
            if (result.isSuccess) {
                currentUser = authRepository.getCurrentUser()
                userRole = "user"
                firestoreUserEmail = null // All users now have Firebase Auth
                firestoreUserName = name
                dataStoreManager.saveSession(email = email, role = "user", name = name)
                onResult(Result.success(Unit))
            } else {
                onResult(Result.failure(result.exceptionOrNull() ?: Exception("Registration failed")))
            }
            isLoading = false
            sessionChecked = true
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try { dataStoreManager.clearSession() } catch (_: Exception) {}
        }
        authRepository.signOut()
        currentUser = null
        userRole = null
        firestoreUserEmail = null
        firestoreUserName = null
        sessionChecked = true
    }

    fun resetPassword(email: String, onResult: (Result<Unit>) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            onResult(result)
            isLoading = false
        }
    }

    fun checkUserSessionIfNeeded() {
        if ((currentUser != null || firestoreUserEmail != null) && userRole == null) {
            restoreSession()
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
