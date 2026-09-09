package com.zayaai.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user",
    val password: String = "", // For Firestore-only users (hash in production!)
    val createdAt: Long = System.currentTimeMillis()
)

class UserRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): String? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.getString("role")
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getUser(uid: String): User? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // âœ… NEW: Get user by email only (for Firestore users)
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // âœ… NEW: Get user by email and password (for Firestore user login)
    suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            if (querySnapshot.documents.isNotEmpty()) {
                val user = querySnapshot.documents[0].toObject(User::class.java)
                // âš ï¸ TODO: In production, use proper password hashing (BCrypt)
                // For now, compare plain text (only for development!)
                if (user != null && user.password == password) {
                    user
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // âœ… NEW: Save new user to Firestore with auto-generated ID
    suspend fun saveNewUserToFirestore(user: User): String {
        return try {
            val documentRef = db.collection("users").add(
                mapOf(
                    "uid" to "",
                    "email" to user.email,
                    "name" to user.name,
                    "role" to user.role,
                    "password" to user.password,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            documentRef.id
        } catch (e: Exception) {
            ""
        }
    }
}

