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

sealed class AuthState {
    object Initializing : AuthState()
    data class Authenticated(val role: String, val displayName: String, val email: String) : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    var authState by mutableStateOf<AuthState>(AuthState.Initializing)
        private set

    var isOnboardingCompleted by mutableStateOf(false)
        private set

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

    /** Display name: uses Firebase displayName, saved name, or email prefix */
    val displayName: String
        get() {
            val fbName = currentUser?.displayName?.takeIf { it.isNotBlank() }
            if (!fbName.isNullOrBlank()) return fbName

            val fsName = firestoreUserName?.takeIf { it.isNotBlank() }
            if (!fsName.isNullOrBlank()) return fsName

            val emailPart = (currentUser?.email ?: firestoreUserEmail)?.takeIf { it.isNotBlank() }
            if (!emailPart.isNullOrBlank()) {
                val namePart = emailPart.substringBefore("@")
                return namePart.split(".").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }

            return "User"
        }

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            try {
                // 1. Read persistent onboarding state
                val onboardingDone = dataStoreManager.isOnboardingCompleted()
                isOnboardingCompleted = onboardingDone

                // 2. Check Firebase Auth session (official secure persistence)
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    currentUser = firebaseUser
                    val savedSession = withContext(Dispatchers.IO) {
                        dataStoreManager.getSavedSession()
                    }

                    // Attempt role fetch with safe fallback (NEVER sign out if role lookup fails)
                    val fetchedRole = withTimeoutOrNull(3000L) {
                        withContext(Dispatchers.IO) {
                            try {
                                authRepository.getUserRole(firebaseUser.uid)
                            } catch (_: Exception) { null }
                        }
                    }

                    val role = fetchedRole ?: savedSession?.role ?: "user"
                    userRole = role

                    val name = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: savedSession?.name?.takeIf { it.isNotBlank() }
                        ?: withTimeoutOrNull(2000L) {
                            withContext(Dispatchers.IO) {
                                try {
                                    authRepository.getUserNameByUid(firebaseUser.uid)
                                } catch (_: Exception) { null }
                            }
                        }
                        ?: firebaseUser.email?.substringBefore("@")
                        ?: "User"

                    firestoreUserName = name
                    firestoreUserEmail = firebaseUser.email

                    // Persist session metadata
                    dataStoreManager.saveSession(
                        email = firebaseUser.email ?: "",
                        role = role,
                        name = name
                    )

                    if (!onboardingDone) {
                        dataStoreManager.setOnboardingCompleted(true)
                        isOnboardingCompleted = true
                    }

                    sessionChecked = true
                    authState = AuthState.Authenticated(role, name, firebaseUser.email ?: "")
                    return@launch
                }

                // 3. Fallback to saved local session in DataStore
                val savedSession = withContext(Dispatchers.IO) {
                    dataStoreManager.getSavedSession()
                }
                if (savedSession != null) {
                    firestoreUserEmail = savedSession.email
                    firestoreUserName = savedSession.name.ifBlank { null }
                    userRole = savedSession.role
                    currentUser = null

                    if (!onboardingDone) {
                        dataStoreManager.setOnboardingCompleted(true)
                        isOnboardingCompleted = true
                    }

                    sessionChecked = true
                    authState = AuthState.Authenticated(savedSession.role, savedSession.name, savedSession.email)
                    return@launch
                }

                // 4. Truly unauthenticated
                currentUser = null
                userRole = null
                firestoreUserEmail = null
                firestoreUserName = null
                authState = AuthState.Unauthenticated
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Session restore error", e)
                val saved = try { dataStoreManager.getSavedSession() } catch (_: Exception) { null }
                if (saved != null) {
                    firestoreUserEmail = saved.email
                    firestoreUserName = saved.name.ifBlank { null }
                    userRole = saved.role
                    authState = AuthState.Authenticated(saved.role, saved.name, saved.email)
                } else {
                    authState = AuthState.Unauthenticated
                }
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
                val role = loginResult.role
                userRole = role
                val firebaseUser = authRepository.getCurrentUser()
                currentUser = firebaseUser
                val name = loginResult.name.ifBlank {
                    firebaseUser?.displayName ?: email.substringBefore("@")
                }
                firestoreUserName = name
                firestoreUserEmail = firebaseUser?.email ?: email

                // Persist session
                dataStoreManager.saveSession(
                    email = email,
                    role = role,
                    name = name
                )
                dataStoreManager.setOnboardingCompleted(true)
                isOnboardingCompleted = true

                authState = AuthState.Authenticated(role, name, email)
                onResult(Result.success(role))
            } else {
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
                firestoreUserEmail = email
                firestoreUserName = name
                dataStoreManager.saveSession(email = email, role = "user", name = name)
                dataStoreManager.setOnboardingCompleted(true)
                isOnboardingCompleted = true
                authState = AuthState.Authenticated("user", name, email)
                onResult(Result.success(Unit))
            } else {
                onResult(Result.failure(result.exceptionOrNull() ?: Exception("Registration failed")))
            }
            isLoading = false
            sessionChecked = true
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(true)
            isOnboardingCompleted = true
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
        authState = AuthState.Unauthenticated
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
        if (authState is AuthState.Initializing) {
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
