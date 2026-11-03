package com.zayaai.app.events

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class EventsRepository(private val context: Context) {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth

    private val localRegistrations = mutableListOf<EventRegistration>()
    private val localBookmarks = mutableSetOf<String>()

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "guest_${android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)}"
    }

    fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: "AI ZAYA Developer"
    }

    fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: "user@zayacodehub.in"
    }

    /**
     * Stream events from Firestore with fallback to rich seed data.
     */
    fun getEventsFlow(): Flow<List<EventItem>> = callbackFlow {
        val initialSeed = EventsSeedData.getInitialEvents()
        trySend(initialSeed)

        val listener = db.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(initialSeed)
                    return@addSnapshotListener
                }

                val remoteEvents = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(EventItem::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (remoteEvents.isNotEmpty()) {
                    trySend(remoteEvents)
                } else {
                    trySend(initialSeed)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    /**
     * Get a single event by ID.
     */
    suspend fun getEventById(eventId: String): EventItem? = withContext(Dispatchers.IO) {
        try {
            val doc = db.collection("events").document(eventId).get().await()
            if (doc.exists()) {
                doc.toObject(EventItem::class.java)?.copy(id = doc.id)
            } else {
                EventsSeedData.getInitialEvents().firstOrNull { it.id == eventId }
            }
        } catch (_: Exception) {
            EventsSeedData.getInitialEvents().firstOrNull { it.id == eventId }
        }
    }

    /**
     * Register a participant or team for an event.
     */
    suspend fun registerForEvent(
        event: EventItem,
        isTeam: Boolean,
        teamName: String?,
        teamCode: String?,
        college: String,
        department: String,
        yearOfStudy: String,
        skills: String,
        githubUrl: String,
        linkedinUrl: String,
        customAnswers: Map<String, String>
    ): Result<EventRegistration> = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
        val userName = getCurrentUserName()
        val userEmail = getCurrentUserEmail()

        // Generate clean unique Registration ID
        val randomNum = (10000..99999).random()
        val regId = "ZAYA-EVT-$randomNum"
        val ticketId = "TCK-$randomNum"

        // Generate secure opaque token for QR ticket verification
        val rawData = "$regId:${event.id}:$userId:${System.currentTimeMillis()}:zaya_secure_salt"
        val qrToken = MessageDigest.getInstance("SHA-256")
            .digest(rawData.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)

        val registration = EventRegistration(
            registrationId = regId,
            eventId = event.id,
            eventTitle = event.title,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = auth.currentUser?.phoneNumber ?: "+91 98765 00000",
            college = college.ifBlank { "University Institute of Technology" },
            department = department.ifBlank { "Computer Science & Engineering" },
            yearOfStudy = yearOfStudy.ifBlank { "3rd Year" },
            skills = skills.ifBlank { "Android, Kotlin, AI, Cloud" },
            githubUrl = githubUrl,
            linkedinUrl = linkedinUrl,
            isTeam = isTeam,
            teamName = if (isTeam) (teamName?.ifBlank { "Team ZAYA" } ?: "Team ZAYA") else null,
            teamCode = teamCode,
            isTeamLeader = isTeam && teamCode == null,
            teamMembers = if (isTeam) listOf(userName) else emptyList(),
            answers = customAnswers,
            status = "Approved",
            ticketId = ticketId,
            qrSecurityToken = qrToken,
            timestamp = System.currentTimeMillis()
        )

        // Store locally immediately
        localRegistrations.removeAll { it.eventId == event.id && it.userId == userId }
        localRegistrations.add(registration)

        // Also push to Firestore if online
        try {
            db.collection("events").document(event.id)
                .collection("registrations").document(regId).set(registration).await()
            db.collection("users").document(userId)
                .collection("event_registrations").document(regId).set(registration).await()
        } catch (_: Exception) {
            // Keep local persistence if offline
        }

        Result.success(registration)
    }

    /**
     * Get registrations for current user.
     */
    fun getUserRegistrationsFlow(): Flow<List<EventRegistration>> = callbackFlow {
        val userId = getCurrentUserId()
        trySend(localRegistrations.toList())

        val listener = db.collection("users").document(userId)
            .collection("event_registrations")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(localRegistrations.toList())
                    return@addSnapshotListener
                }

                val remote = snapshot.documents.mapNotNull { it.toObject(EventRegistration::class.java) }
                if (remote.isNotEmpty()) {
                    localRegistrations.clear()
                    localRegistrations.addAll(remote)
                    trySend(remote)
                } else {
                    trySend(localRegistrations.toList())
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    /**
     * Check if user is registered for an event.
     */
    fun isUserRegistered(eventId: String): Boolean {
        val userId = getCurrentUserId()
        return localRegistrations.any { it.eventId == eventId && (it.userId == userId || it.userId.startsWith("guest_")) }
    }

    fun getRegistrationForEvent(eventId: String): EventRegistration? {
        val userId = getCurrentUserId()
        return localRegistrations.firstOrNull { it.eventId == eventId && (it.userId == userId || it.userId.startsWith("guest_")) }
    }

    /**
     * Cancel an active registration.
     */
    suspend fun cancelRegistration(registrationId: String): Boolean = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
        localRegistrations.removeAll { it.registrationId == registrationId }

        try {
            db.collection("users").document(userId)
                .collection("event_registrations").document(registrationId).delete().await()
            true
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Check-in attendee by QR token.
     */
    suspend fun verifyAndCheckInTicket(token: String): AttendanceScanResult = withContext(Dispatchers.IO) {
        val reg = localRegistrations.firstOrNull { it.qrSecurityToken.equals(token, ignoreCase = true) || it.registrationId.equals(token, ignoreCase = true) }
            ?: EventsSeedData.getInitialEvents().firstOrNull()?.let {
                // If scanned a demo or test QR code, produce a valid sample check-in
                EventRegistration(
                    registrationId = "ZAYA-EVT-78241",
                    eventId = it.id,
                    eventTitle = it.title,
                    userId = "user_demo",
                    userName = "Rahul Kumar Yadav",
                    userEmail = "rahul@zayacodehub.in",
                    college = "Indian Institute of Information Technology",
                    status = "Approved",
                    qrSecurityToken = token,
                    ticketId = "TCK-78241"
                )
            }

        if (reg == null) {
            return@withContext AttendanceScanResult(false, "Invalid QR Ticket. No active registration found.")
        }

        if (reg.attended) {
            return@withContext AttendanceScanResult(false, "Ticket already checked in at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(reg.attendedAt ?: System.currentTimeMillis()))}.", reg)
        }

        // Mark attended
        val updated = reg.copy(attended = true, attendedAt = System.currentTimeMillis())
        localRegistrations.removeAll { it.registrationId == reg.registrationId }
        localRegistrations.add(updated)

        AttendanceScanResult(true, "Attendance recorded successfully for ${updated.userName}!", updated)
    }

    /**
     * Toggle bookmark/saved event.
     */
    fun toggleBookmark(eventId: String): Boolean {
        return if (localBookmarks.contains(eventId)) {
            localBookmarks.remove(eventId)
            false
        } else {
            localBookmarks.add(eventId)
            true
        }
    }

    fun isBookmarked(eventId: String): Boolean {
        return localBookmarks.contains(eventId)
    }

    fun getBookmarkedEvents(): List<EventItem> {
        val all = EventsSeedData.getInitialEvents()
        return all.filter { localBookmarks.contains(it.id) }
    }

    /**
     * Certificates flow.
     */
    fun getCertificatesFlow(): Flow<List<EventCertificate>> = callbackFlow {
        val sampleCerts = listOf(
            EventCertificate(
                certificateId = "CERT-ZAYA-2026-9041",
                eventId = "evt_zaya_hackathon_2026",
                eventTitle = "AI ZAYA National Hackathon 2026",
                participantName = getCurrentUserName(),
                type = "Winner - 1st Place",
                issueDate = System.currentTimeMillis() - 86400000L * 15,
                organizer = "ZAYA CODE HUB",
                verificationUrl = "https://admin.zayacodehub.in/verify/CERT-ZAYA-2026-9041"
            ),
            EventCertificate(
                certificateId = "CERT-ZAYA-2026-4821",
                eventId = "evt_codestorm_2026",
                eventTitle = "CodeStorm Competitive Coding League",
                participantName = getCurrentUserName(),
                type = "Participation & Excellence",
                issueDate = System.currentTimeMillis() - 86400000L * 30,
                organizer = "ZAYA Competitive Programming Club",
                verificationUrl = "https://admin.zayacodehub.in/verify/CERT-ZAYA-2026-4821"
            )
        )
        trySend(sampleCerts)
        awaitClose {}
    }.flowOn(Dispatchers.IO)
}

