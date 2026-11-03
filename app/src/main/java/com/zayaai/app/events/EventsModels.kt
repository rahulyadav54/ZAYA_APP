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
    val organizer: String = "ZAYA CODE HUB",
    val organizerLogo: String? = null,
    val isVerifiedOrganizer: Boolean = true,
    val posterUrl: String = "",
    val startDate: Long = System.currentTimeMillis() + 86400000L * 7,
    val endDate: Long = System.currentTimeMillis() + 86400000L * 9,
    val registrationDeadline: Long = System.currentTimeMillis() + 86400000L * 5,
    val mode: String = "Hybrid", // "Online", "Offline", "Hybrid"
    val venue: String = "ZAYA Tech Campus, Innovation Auditorium & Discord",
    val locationUrl: String? = "https://maps.google.com/?q=Tech+Auditorium",
    val meetingLink: String? = "https://meet.google.com/zaya-hub",
    val isFree: Boolean = true,
    val price: Double = 0.0,
    val currency: String = "₹",
    val prizePool: String = "₹1,50,000",
    val eligibility: String = "Open to all students, developers, and tech enthusiasts",
    val minTeamSize: Int = 1,
    val maxTeamSize: Int = 4,
    val capacity: Int = 500,
    val registeredCount: Int = 184,
    val description: String = "",
    val rules: List<String> = emptyList(),
    val agenda: List<EventAgendaItem> = emptyList(),
    val faqs: List<EventFaqItem> = emptyList(),
    val contactEmail: String = "events@zayacodehub.in",
    val contactPhone: String? = "+91 98765 43210",
    val websiteUrl: String? = "https://admin.zayacodehub.in",
    val customQuestions: List<EventCustomQuestion> = emptyList(),
    val status: String = "Upcoming", // "Upcoming", "Ongoing", "Completed", "Cancelled"
    val featured: Boolean = false
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

