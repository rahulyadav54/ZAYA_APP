package com.zayaai.app.events

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zayaai.app.AIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    val repository = EventsRepository(application.applicationContext)

    private val _events = MutableStateFlow<List<EventItem>>(EventsSeedData.getInitialEvents())
    val events: StateFlow<List<EventItem>> = _events.asStateFlow()

    private val _registrations = MutableStateFlow<List<EventRegistration>>(emptyList())
    val registrations: StateFlow<List<EventRegistration>> = _registrations.asStateFlow()

    private val _certificates = MutableStateFlow<List<EventCertificate>>(emptyList())
    val certificates: StateFlow<List<EventCertificate>> = _certificates.asStateFlow()

    // Filters & Search
    var selectedCategory by mutableStateOf("All")
    var searchQuery by mutableStateOf("")
    var filterMode by mutableStateOf<String?>(null) // null = all, "Online", "Offline", "Hybrid"
    var filterFreeOnly by mutableStateOf(false)

    // Current View / Dialog State
    var selectedEvent by mutableStateOf<EventItem?>(null)
    var selectedTicket by mutableStateOf<EventRegistration?>(null)
    var selectedCertificate by mutableStateOf<EventCertificate?>(null)
    var isRegistering by mutableStateOf(false)
    var registrationSuccessMessage by mutableStateOf<String?>(null)

    // AI Event Assistant
    var aiEventQuestion by mutableStateOf("")
    var aiEventAnswer by mutableStateOf<String?>(null)
    var isAiAnswering by mutableStateOf(false)

    // Attendance Scan Result
    var lastScanResult by mutableStateOf<AttendanceScanResult?>(null)

    init {
        loadEvents()
        loadRegistrations()
        loadCertificates()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.getEventsFlow().collect { list ->
                _events.value = list
            }
        }
    }

    private fun loadRegistrations() {
        viewModelScope.launch {
            repository.getUserRegistrationsFlow().collect { list ->
                _registrations.value = list
            }
        }
    }

    private fun loadCertificates() {
        viewModelScope.launch {
            repository.getCertificatesFlow().collect { list ->
                _certificates.value = list
            }
        }
    }

    fun getFilteredEvents(): List<EventItem> {
        return _events.value.filter { event ->
            val matchCategory = selectedCategory == "All" || event.category.equals(selectedCategory, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.organizer.contains(searchQuery, ignoreCase = true) ||
                    event.venue.contains(searchQuery, ignoreCase = true) ||
                    event.category.contains(searchQuery, ignoreCase = true)
            val matchMode = filterMode == null || event.mode.equals(filterMode, ignoreCase = true)
            val matchFee = !filterFreeOnly || event.isFree

            matchCategory && matchSearch && matchMode && matchFee
        }
    }

    fun getFeaturedEvents(): List<EventItem> {
        return _events.value.filter { it.featured || it.registeredCount > 150 }
    }

    fun getUpcomingEvents(): List<EventItem> {
        return _events.value.sortedBy { it.startDate }
    }

    fun isUserRegistered(eventId: String): Boolean {
        return repository.isUserRegistered(eventId) || _registrations.value.any { it.eventId == eventId }
    }

    fun getRegistrationForEvent(eventId: String): EventRegistration? {
        return _registrations.value.firstOrNull { it.eventId == eventId } ?: repository.getRegistrationForEvent(eventId)
    }

    fun toggleBookmark(eventId: String): Boolean {
        return repository.toggleBookmark(eventId)
    }

    fun isBookmarked(eventId: String): Boolean {
        return repository.isBookmarked(eventId)
    }

    fun getSavedEvents(): List<EventItem> {
        return _events.value.filter { repository.isBookmarked(it.id) }
    }

    fun submitRegistration(
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
        customAnswers: Map<String, String>,
        onSuccess: (EventRegistration) -> Unit
    ) {
        viewModelScope.launch {
            isRegistering = true
            val result = repository.registerForEvent(
                event = event,
                isTeam = isTeam,
                teamName = teamName,
                teamCode = teamCode,
                college = college,
                department = department,
                yearOfStudy = yearOfStudy,
                skills = skills,
                githubUrl = githubUrl,
                linkedinUrl = linkedinUrl,
                customAnswers = customAnswers
            )
            isRegistering = false

            result.onSuccess { reg ->
                registrationSuccessMessage = "You have successfully registered for ${event.title}!"
                selectedTicket = reg
                onSuccess(reg)
            }
        }
    }

    fun cancelRegistration(registrationId: String) {
        viewModelScope.launch {
            repository.cancelRegistration(registrationId)
        }
    }

    fun verifyTicket(token: String) {
        viewModelScope.launch {
            lastScanResult = repository.verifyAndCheckInTicket(token)
        }
    }

    fun askAiAboutEvent(event: EventItem, question: String) {
        if (question.isBlank() || isAiAnswering) return
        viewModelScope.launch {
            isAiAnswering = true
            aiEventQuestion = question

            val groundTruthContext = """
Event Title: ${event.title}
Category: ${event.category}
Organizer: ${event.organizer}
Dates: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(event.startDate))} to ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(event.endDate))}
Registration Deadline: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(event.registrationDeadline))}
Mode: ${event.mode}
Venue: ${event.venue}
Fee: ${if (event.isFree) "Free" else "${event.currency} ${event.price}"}
Prize Pool: ${event.prizePool}
Team Size: ${event.minTeamSize} to ${event.maxTeamSize} members
Eligibility: ${event.eligibility}
Rules:
${event.rules.joinToString("\n") { "- $it" }}
Agenda:
${event.agenda.joinToString("\n") { "${it.time}: ${it.title} (${it.description})" }}
FAQs:
${event.faqs.joinToString("\n") { "Q: ${it.question}\nA: ${it.answer}" }}
Contact Email: ${event.contactEmail}
            """.trimIndent()

            val prompt = """You are AI ZAYA's official Event Assistant. Answer the user's question accurately using ONLY the event information provided below. Do not invent details. If the information is not present, politely say so.

Event Ground Truth:
$groundTruthContext

User Question: $question"""

            try {
                val answer = AIService.generate(prompt, "You are a concise, helpful Event Assistant.")
                aiEventAnswer = answer
            } catch (e: Exception) {
                aiEventAnswer = "Sorry, I could not retrieve the event details right now. Please check your connection."
            } finally {
                isAiAnswering = false
            }
        }
    }
}

