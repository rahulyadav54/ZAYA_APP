package com.zayaai.app.events

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class EventAgendaItem(
    val time: String = "",
    val title: String = "",
    val description: String = ""
)

@Keep
data class EventFaqItem(
    val question: String = "",
    val answer: String = ""
)

@Keep
data class EventCustomQuestion(
    val id: String = "",
    val label: String = "",
    val type: String = "text", // "text", "dropdown", "url", "checkbox"
    val options: List<String> = emptyList(),
    val required: Boolean = true
)

@Keep
data class EventItem(
    val id: String = "",
    val title: String = "",
    val tagline: String = "",
    val category: String = "Hackathons", // "Hackathons", "Coding Competitions", "Workshops", "Webinars", "Conferences", "Bootcamps", "Tech Festivals"
    val organizer: String = "",
    val organizerLogo: String? = null,
    val isVerifiedOrganizer: Boolean = false,
    val posterUrl: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val registrationDeadline: Long = 0L,
    val mode: String = "Online", // "Online", "In-Person", "Hybrid"
    val venue: String = "",
    val locationUrl: String? = null,
    val meetingLink: String? = null,
    val isFree: Boolean = true,
    val price: Double = 0.0,
    val currency: String = "₹",
    val prizePool: String = "",
    val eligibility: String = "",
    val minTeamSize: Int = 1,
    val maxTeamSize: Int = 1,
    val capacity: Int = 0, // 0 = unlimited
    val registeredCount: Int = 0,
    val description: String = "",
    val rules: List<String> = emptyList(),
    val agenda: List<EventAgendaItem> = emptyList(),
    val faqs: List<EventFaqItem> = emptyList(),
    val contactEmail: String = "",
    val contactPhone: String? = null,
    val websiteUrl: String? = null,
    val customQuestions: List<EventCustomQuestion> = emptyList(),
    val status: String = "Upcoming", // "Upcoming", "Ongoing", "Completed", "Cancelled"
    val featured: Boolean = false,
    val published: Boolean = true
)

@Keep
data class TeamMemberInfo(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val yearOfStudy: String = ""
)

@Keep
data class EventRegistration(
    val registrationId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val college: String = "",
    val department: String = "",
    val yearOfStudy: String = "",
    val skills: String = "",
    val githubUrl: String = "",
    val linkedinUrl: String = "",
    val isTeam: Boolean = false,
    val teamName: String? = null,
    val teamCode: String? = null,
    val isTeamLeader: Boolean = false,
    val teamMembers: List<String> = emptyList(),
    val teamMembersDetails: List<TeamMemberInfo> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val status: String = "Approved", // "Approved", "Pending", "Waitlisted", "Cancelled", "Attended"
    val ticketId: String = "",
    val qrSecurityToken: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val attended: Boolean = false,
    val attendedAt: Long? = null
)

@Keep
data class EventTicket(
    val ticketId: String = "",
    val registrationId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val participantName: String = "",
    val isTeam: Boolean = false,
    val teamName: String? = null,
    val eventDate: String = "",
    val venue: String = "",
    val status: String = "Valid", // "Valid", "Used", "Revoked"
    val qrOpaqueToken: String = ""
)

@Keep
data class EventCertificate(
    val certificateId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val participantName: String = "",
    val type: String = "Participation", // "Participation", "Winner", "Runner-up", "Special Recognition"
    val issueDate: Long = System.currentTimeMillis(),
    val organizer: String = "ZAYA CODE HUB",
    val verificationUrl: String = "https://admin.zayacodehub.in/verify"
)

@Keep
data class EventAnnouncement(
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val priority: String = "Normal" // "High", "Normal"
)

data class AttendanceScanResult(
    val success: Boolean,
    val message: String,
    val registration: EventRegistration? = null
)

