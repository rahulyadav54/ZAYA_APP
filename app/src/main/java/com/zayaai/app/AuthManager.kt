package com.zayaai.app

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth

    fun getSignInIntent(): Intent {
        // We will try to get the string resource, and if it fails (not yet generated),
        // we use a placeholder to allow compilation.
        val defaultClientId = "493304702545-6fhbfpd1t3fbiln9fqgvi19rp8uugtk2.apps.googleusercontent.com"
        val webClientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else defaultClientId
        } catch (e: Exception) {
            defaultClientId
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(email: String, pass: String): Result<Boolean> {
        return try {
            // Validate inputs
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (pass.isBlank()) {
                return Result.failure(Exception("Password cannot be empty"))
            }
            if (pass.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }
            if (!email.contains("@")) {
                return Result.failure(Exception("Invalid email format"))
            }
            
            auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            Result.success(true)
        } catch (e: Exception) {
            // Provide user-friendly error messages
            val message = when {
                e.message?.contains("already", ignoreCase = true) == true -> "This email is already registered. Please login instead."
                e.message?.contains("invalid email", ignoreCase = true) == true -> "Invalid email address format."
                e.message?.contains("weak password", ignoreCase = true) == true -> "Password is too weak. Use uppercase, numbers, and special characters."
                e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                else -> e.message ?: "Registration failed. Please try again."
            }
            Result.failure(Exception(message))
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<Boolean> {
        return try {
            // Validate inputs before Firebase call
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (pass.isBlank()) {
                return Result.failure(Exception("Password cannot be empty"))
            }
            if (!email.contains("@")) {
                return Result.failure(Exception("Invalid email format"))
            }
            
            auth.signInWithEmailAndPassword(email.trim(), pass).await()
            Result.success(true)
        } catch (e: Exception) {
            // Provide more user-friendly error messages
            val message = when {
                e.message?.contains("no user record", ignoreCase = true) == true -> "User not found. Please register first."
                e.message?.contains("invalid password", ignoreCase = true) == true -> "Incorrect password. Please try again."
                e.message?.contains("user disabled", ignoreCase = true) == true -> "This account has been disabled."
                e.message?.contains("malformed", ignoreCase = true) == true -> "Invalid credentials format. Please check your input."
                e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your internet connection."
                else -> e.message ?: "Login failed. Please try again."
            }
            Result.failure(Exception(message))
        }
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
    }
}
