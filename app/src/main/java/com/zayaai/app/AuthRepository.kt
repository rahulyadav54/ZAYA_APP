package com.zayaai.app

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser

data class LoginResult(val role: String, val name: String)

class AuthRepository(
    private val authManager: AuthManager,
    private val userRepository: UserRepository
) {
    fun getCurrentUser(): FirebaseUser? = authManager.getCurrentUser()

    suspend fun login(email: String, pass: String): Result<LoginResult> {
        // ALL users login via Firebase Auth (students + admins)
        val firebaseResult = authManager.loginUser(email, pass)
        if (firebaseResult.isSuccess) {
            val user = authManager.getCurrentUser()
            if (user != null) {
                val role = userRepository.getUserRole(user.uid) ?: "user"
                val name = user.displayName ?: userRepository.getUser(user.uid)?.name ?: "User"
                return Result.success(LoginResult(role = role, name = name))
            }
        }
        return Result.failure(firebaseResult.exceptionOrNull() ?: Exception("Invalid email or password"))
    }

    suspend fun register(name: String, email: String, pass: String): Result<Unit> {
        // Register ALL users via Firebase Auth so security rules work
        return try {
            val authResult = authManager.registerUser(email, pass)
            if (authResult.isFailure) {
                return Result.failure(authResult.exceptionOrNull() ?: Exception("Registration failed"))
            }
            
            val firebaseUser = authManager.getCurrentUser()
                ?: return Result.failure(Exception("User not found after registration"))
            
            // Save user profile to Firestore with Firebase Auth UID
            val newUser = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                role = "user",
                password = "", // Don't store password - Firebase Auth handles it
                createdAt = System.currentTimeMillis()
            )
            userRepository.saveUserToFirestore(newUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<String> {
        val result = authManager.signInWithGoogle(account)
        return if (result.isSuccess) {
            val user = authManager.getCurrentUser()
            if (user != null) {
                var role = userRepository.getUserRole(user.uid)
                if (role == null) {
                    val newUser = User(
                        uid = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: "Google User",
                        role = "user"
                    )
                    userRepository.saveUserToFirestore(newUser)
                    role = "user"
                }
                Result.success(role)
            } else {
                Result.failure(Exception("User not found after Google Sign-in"))
            }
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Google login failed"))
        }
    }

    suspend fun getUserRole(uid: String): String? = userRepository.getUserRole(uid)

    suspend fun getUserNameByUid(uid: String): String? = userRepository.getUser(uid)?.name

    suspend fun getUserNameByEmail(email: String): String? = userRepository.getUserByEmail(email)?.name

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val result = authManager.resetPassword(email)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Password reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGoogleSignInIntent(): android.content.Intent = authManager.getSignInIntent()

    fun signOut() {
        authManager.signOut()
    }
}
