package com.zayaai.app.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.zayaai.app.MarkdownText
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventsHubScreen(
    viewModel: EventsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Explore", "Saved", "My Events", "Certificates")

    val events by viewModel.events.collectAsState()
    val registrations by viewModel.registrations.collectAsState()
    val certificates by viewModel.certificates.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF0F111E) else Color(0xFFFFFFFF))
                    .statusBarsPadding()
            ) {
                // Dedicated Events Hub Header (Clean responsive layout, no Pro badge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Center/Left: Title & Subtitle occupying remaining space
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Events Hub",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Discover. Register. Participate. Achieve.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            ),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: Attendance QR Scanner Icon
                    IconButton(
                        onClick = { showScannerDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2138) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = "Attendance Scanner",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right: Notification Bell Icon
                    IconButton(
                        onClick = { showNotificationDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2138) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = "Announcements",
                            tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDark) Color(0xFF0F111E) else Color(0xFFFFFFFF),
                    contentColor = Color(0xFF7C3AED),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF7C3AED),
                            height = 3.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(color = if (isDark) Color(0xFF232544) else Color(0xFFE2E8F0))
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    color = if (selectedTab == index) {
                                        if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1)
                                    } else {
                                        if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF0B0C16) else Color(0xFFF8FAFC)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> ExploreEventsTab(viewModel = viewModel)
                1 -> SavedEventsTab(viewModel = viewModel)
                2 -> MyEventsTab(viewModel = viewModel)
                3 -> CertificatesTab(viewModel = viewModel)
            }
        }
    }

    // Modal dialogs
    viewModel.selectedEvent?.let { event ->
        EventDetailsDialog(
            event = event,
            viewModel = viewModel,
            onDismiss = { viewModel.selectedEvent = null },
            onAskAi = {
                showAiAssistantDialog = true
            }
        )
    }

    viewModel.selectedTicket?.let { reg ->
        DigitalTicketDialog(
            registration = reg,
            onDismiss = { viewModel.selectedTicket = null }
        )
    }

    viewModel.selectedCertificate?.let { cert ->
        CertificateDialog(
            certificate = cert,
            onDismiss = { viewModel.selectedCertificate = null }
        )
    }

    
    var liveAnnouncementsList by remember { mutableStateOf<List<EventAnnouncement>>(emptyList()) }
    var isLoadingAnnouncements by remember { mutableStateOf(false) }

    LaunchedEffect(showNotificationDialog) {
        if (showNotificationDialog) {
            isLoadingAnnouncements = true
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("announcements")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener { snap ->
                        liveAnnouncementsList = snap.documents.mapNotNull { it.toObject(EventAnnouncement::class.java) }
                        isLoadingAnnouncements = false
                    }
                    .addOnFailureListener {
                        isLoadingAnnouncements = false
                    }
            } catch (_: Exception) {
                isLoadingAnnouncements = false
            }
        }
    }

    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF141629) else Color.White,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Live Announcements", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
                                Text("Real-time alerts from organizers", fontSize = 11.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                            }
                        }
                        IconButton(onClick = { showNotificationDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isLoadingAnnouncements) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF7C3AED), modifier = Modifier.size(28.dp))
                        }
                    } else if (liveAnnouncementsList.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF191C36) else Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No New Announcements", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Broadcasts, schedule updates, and deadline reminders published from the admin portal will appear here in real-time.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(liveAnnouncementsList) { ann ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF191C36) else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF282C4C) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(ann.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                            if (ann.priority == "High") {
                                                Box(
                                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEF4444).copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("URGENT", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(ann.message, fontSize = 12.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569), lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showNotificationDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showScannerDialog) {
        OrganizerScannerDialog(
            viewModel = viewModel,
            onDismiss = { showScannerDialog = false }
        )
    }

    if (showAiAssistantDialog && viewModel.selectedEvent != null) {
        EventAiAssistantDialog(
            event = viewModel.selectedEvent!!,
            viewModel = viewModel,
            onDismiss = { showAiAssistantDialog = false }
        )
    }
}

// ─── Tab 1: Explore Events ───

@Composable
private fun ExploreEventsTab(viewModel: EventsViewModel) {
    val isDark = LocalIsDarkTheme.current
    val allEvents by viewModel.events.collectAsState()
    val categories = listOf("All", "Hackathons", "Coding Competitions", "Workshops", "Conferences", "Bootcamps")
    val filteredEvents = remember(allEvents, viewModel.selectedCategory, viewModel.searchQuery, viewModel.filterMode, viewModel.filterFreeOnly) {
        viewModel.getFilteredEvents()
    }
    val featuredEvents = remember(allEvents) {
        viewModel.getFeaturedEvents()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Filter Bar
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF16182B) else Color(0xFFFFFFFF),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                    shadowElevation = if (isDark) 0.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search hackathons, coding contests, colleges...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF7C3AED)
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode filter chips (Online, Offline, Hybrid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(null to "All Modes", "Online" to "Online", "Offline" to "In-Person", "Hybrid" to "Hybrid").forEach { (mode, label) ->
                        val isSelected = viewModel.filterMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.filterMode = mode },
                            label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C3AED).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF7C3AED)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF7C3AED) else if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }
        }

        // Category Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = viewModel.selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            Color(0xFF7C3AED)
                        } else {
                            if (isDark) Color(0xFF16182B) else Color(0xFFFFFFFF)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF7C3AED) else if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectedCategory = category }
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Featured Events Section
        if (featuredEvents.isNotEmpty() && viewModel.searchQuery.isBlank() && viewModel.selectedCategory == "All") {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Stars,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Featured Opportunities",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(featuredEvents) { event ->
                            FeaturedEventHeroCard(
                                event = event,
                                isBookmarked = viewModel.isBookmarked(event.id),
                                onBookmarkToggle = { viewModel.toggleBookmark(event.id) },
                                onClick = { viewModel.selectedEvent = event }
                            )
                        }
                    }
                }
            }
        }

        // All / Filtered Events Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${viewModel.selectedCategory} (${filteredEvents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                Text(
                    text = "Sorted by Date",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
            }
        }

        // Event List
        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.EventBusy,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No events available right now.",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check back soon or create new events from the organizer portal.",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EventCard(
                        event = event,
                        isBookmarked = viewModel.isBookmarked(event.id),
                        isRegistered = viewModel.isUserRegistered(event.id),
                        onBookmarkToggle = { viewModel.toggleBookmark(event.id) },
                        onClick = { viewModel.selectedEvent = event }
                    )
                }
            }
        }
    }
}

// ─── Tab 2: Saved Events ───

@Composable
private fun SavedEventsTab(viewModel: EventsViewModel) {
    val isDark = LocalIsDarkTheme.current
    val allEvents by viewModel.events.collectAsState()
    val savedEvents = remember(allEvents) { viewModel.getSavedEvents() }

    if (savedEvents.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No Saved Events Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the bookmark icon on any hackathon, workshop, or conference to save it here for quick access.",
                    fontSize = 12.5.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(savedEvents, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    isBookmarked = true,
                    isRegistered = viewModel.isUserRegistered(event.id),
                    onBookmarkToggle = { viewModel.toggleBookmark(event.id) },
                    onClick = { viewModel.selectedEvent = event }
                )
            }
        }
    }
}

// ─── Tab 3: My Events ───

@Composable
private fun MyEventsTab(viewModel: EventsViewModel) {
    val isDark = LocalIsDarkTheme.current
    val registrations by viewModel.registrations.collectAsState()

    if (registrations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED).copy(alpha = 0.6f),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "You Haven't Registered for Any Events",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Browse the Explore tab to find hackathons, tech workshops, and coding leagues to register for free.",
                    fontSize = 12.5.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(registrations, key = { it.registrationId }) { reg ->
                MyRegistrationCard(
                    registration = reg,
                    onViewTicket = { viewModel.selectedTicket = reg },
                    onCancel = { viewModel.cancelRegistration(reg.registrationId) }
                )
            }
        }
    }
}

// ─── Tab 4: Certificates ───

@Composable
private fun CertificatesTab(viewModel: EventsViewModel) {
    val isDark = LocalIsDarkTheme.current
    val certificates by viewModel.certificates.collectAsState()

    LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF16182B) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Verified Event Credentials",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "All issued certificates carry cryptographic verification IDs traceable on admin.zayacodehub.in.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        items(certificates, key = { it.certificateId }) { cert ->
            CertificateItemCard(
                certificate = cert,
                onClick = { viewModel.selectedCertificate = cert }
            )
        }
    }
}

// ─── UI Cards ───

@Composable
private fun FeaturedEventHeroCard(
    event: EventItem,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E3258) else Color(0xFFE2E8F0)),
        shadowElevation = if (isDark) 0.dp else 2.dp,
        modifier = Modifier
            .width(310.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF38BDF8))
                        )
                    )
                    .padding(12.dp)
            ) {
                if (event.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }
                // Category pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.category.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Bookmark icon
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isBookmarked) Color(0xFFF59E0B) else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Prize Pool Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Prize: ${event.prizePool}",
                            color = Color(0xFF0F172A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By ${event.organizer}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = if (isDark) Color(0xFF232544) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormat.format(Date(event.startDate)),
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.mode,
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: EventItem,
    isBookmarked: Boolean,
    isRegistered: Boolean,
    onBookmarkToggle: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF141629) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF232644) else Color(0xFFE2E8F0)),
        shadowElevation = if (isDark) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (event.posterUrl.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(bottom = 12.dp)
                ) {
                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (event.category) {
                                "Hackathons" -> Icons.Rounded.Code
                                "Coding Competitions" -> Icons.Rounded.Terminal
                                "Workshops" -> Icons.Rounded.School
                                "Conferences" -> Icons.Rounded.Groups
                                else -> Icons.Rounded.Event
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = event.category,
                                    fontSize = 10.sp,
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isRegistered) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "REGISTERED",
                                        fontSize = 9.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = event.organizer,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isBookmarked) Color(0xFFF59E0B) else if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF1F223C) else Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateFormat.format(Date(event.startDate)),
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = event.mode,
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )
                    }

                    if (event.prizePool.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = event.prizePool,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                // Free or Price
                Text(
                    text = if (event.isFree) "FREE" else "${event.currency} ${event.price}",
                    color = if (event.isFree) Color(0xFF10B981) else Color(0xFF6366F1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MyRegistrationCard(
    registration: EventRegistration,
    onViewTicket: () -> Unit,
    onCancel: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
        shadowElevation = if (isDark) 0.dp else 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = registration.registrationId,
                        color = Color(0xFF7C3AED),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (registration.attended) Color(0xFF38BDF8).copy(alpha = 0.15f)
                            else Color(0xFF10B981).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (registration.attended) "ATTENDED" else "CONFIRMED",
                        color = if (registration.attended) Color(0xFF0284C7) else Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = registration.eventTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            if (registration.isTeam) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Team: ${registration.teamName ?: "My Team"} (Code: ${registration.teamCode ?: "LEADER"})",
                        fontSize = 11.5.sp,
                        color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Registered: ${dateFormat.format(Date(registration.timestamp))}",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF20233B) else Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text(
                        text = "Cancel Registration",
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp
                    )
                }

                Button(
                    onClick = onViewTicket,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Digital Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CertificateItemCard(
    certificate: EventCertificate,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
        shadowElevation = if (isDark) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = certificate.eventTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = certificate.type,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ID: ${certificate.certificateId} • ${dateFormat.format(Date(certificate.issueDate))}",
                    fontSize = 10.5.sp,
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "View",
                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Event Details Dialog ───

@Composable
private fun EventDetailsDialog(
    event: EventItem,
    viewModel: EventsViewModel,
    onDismiss: () -> Unit,
    onAskAi: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm a", Locale.getDefault())
    val isRegistered = viewModel.isUserRegistered(event.id)
    var showRegistrationForm by remember { mutableStateOf(false) }
    val eventRegistrations by viewModel.getEventRegistrationsFlow(event.id).collectAsState(initial = emptyList())
    val registeredTeams = eventRegistrations.filter { it.isTeam || !it.teamName.isNullOrBlank() }
    val totalAttendeesCount = eventRegistrations.sumOf { 1 + it.teamMembersDetails.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF0B0C16) else Color(0xFFFFFFFF)
        ) {
            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF06B6D4))
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.35f))
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }

                            IconButton(
                                onClick = { viewModel.toggleBookmark(event.id) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.35f))
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isBookmarked(event.id)) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = if (viewModel.isBookmarked(event.id)) Color(0xFFF59E0B) else Color.White
                                )
                            }
                        }

                        if (event.posterUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shadowElevation = 4.dp
                            ) {
                                AsyncImage(
                                    model = event.posterUrl,
                                    contentDescription = "Event Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event.category.uppercase(),
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                lineHeight = 27.sp
                            ),
                            color = Color.White
                        )
                    }
                },
                bottomBar = {
                    Surface(
                        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (event.isFree) "FREE ENTRY" else "${event.currency} ${event.price}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (event.isFree) Color(0xFF10B981) else Color(0xFF6366F1)
                                )
                                val seatText = if (event.capacity > 0) {
                                    "${event.registeredCount} / ${event.capacity} seats registered"
                                } else {
                                    "${event.registeredCount} registered"
                                }
                                Text(
                                    text = seatText,
                                    fontSize = 11.5.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            val userReg = viewModel.getRegistrationForEvent(event.id)
                            val isRegistered = userReg != null
                            val isClosed = (event.registrationDeadline > 0 && System.currentTimeMillis() > event.registrationDeadline) ||
                                    (event.capacity > 0 && event.registeredCount >= event.capacity)

                            if (isRegistered && userReg?.status == "Approved") {
                                Button(
                                    onClick = {
                                        viewModel.selectedTicket = userReg
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View Ticket", fontWeight = FontWeight.Bold)
                                }
                            } else if (isRegistered && userReg?.status == "Pending") {
                                Surface(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Under Review",
                                        color = Color(0xFFD97706),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        fontSize = 12.5.sp
                                    )
                                }
                            } else if (isClosed) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Registration Closed", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { showRegistrationForm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Register Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Organizer Row + Ask AI Button
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7C3AED).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.CorporateFare, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = event.organizer,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDark) Color.White else Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Rounded.Verified, contentDescription = "Verified", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                                    }
                                    Text("Official Organizer", fontSize = 11.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                                }
                            }

                            // AI Assistant Button
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onAskAi() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ask AI", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                }
                            }
                        }
                    }

                    // Key Info Cards (Date, Mode, Venue, Prize)
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF14162B) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Event Date & Time", fontSize = 10.5.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                                        Text(dateFormat.format(Date(event.startDate)), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF0F172A))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Registration Deadline", fontSize = 10.5.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                                        Text(dateFormat.format(Date(event.registrationDeadline)), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Venue / Platform (${event.mode})", fontSize = 10.5.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                                        Text(event.venue, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF0F172A))
                                    }
                                }

                                if (event.prizePool.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Prize Pool & Grants", fontSize = 10.5.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                                            Text(event.prizePool, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- REALTIME REGISTERED TEAMS & PARTICIPANTS ROSTER ---
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color(0xFF14162B) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.5.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "REGISTERED TEAMS & ATTENDEES",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF7C3AED),
                                            letterSpacing = 0.8.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF7C3AED).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${registeredTeams.size} Teams",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF7C3AED),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$totalAttendeesCount Attendees",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                if (eventRegistrations.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Rounded.Groups,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "No teams registered yet. Be the first to register!",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        eventRegistrations.forEach { reg ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isDark) Color(0xFF1B1E38) else Color(0xFFFFFFFF),
                                                border = BorderStroke(1.dp, if (isDark) Color(0xFF282C4E) else Color(0xFFE2E8F0)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = if (reg.isTeam) "🛡️ ${reg.teamName ?: "Team ${reg.userName}"}" else "👤 ${reg.userName} (Solo)",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (reg.isTeam) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    color = Color(0xFF6366F1).copy(alpha = 0.15f)
                                                                ) {
                                                                    Text(
                                                                        "${1 + reg.teamMembersDetails.size} Members",
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color(0xFF6366F1),
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        if (reg.attended) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                            ) {
                                                                Text("Checked In ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                            }
                                                        }
                                                    }

                                                    if (reg.college.isNotBlank()) {
                                                        Text(
                                                            text = "🏛️ ${reg.college}${if (reg.department.isNotBlank()) " • ${reg.department}" else ""}",
                                                            fontSize = 11.5.sp,
                                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                                        )
                                                    }

                                                    // Team Members List
                                                    if (reg.isTeam && reg.teamMembersDetails.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Team Members:",
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF7C3AED)
                                                        )
                                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            // Leader
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = Color(0xFF7C3AED).copy(alpha = 0.12f),
                                                                border = BorderStroke(0.5.dp, Color(0xFF7C3AED).copy(alpha = 0.3f)),
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                    Text("👑 ${reg.userName} (Leader)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED))
                                                                    if (reg.department.isNotBlank()) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text("• ${reg.department} (${reg.yearOfStudy})", fontSize = 10.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                                                                    }
                                                                }
                                                            }
                                                            // Other members
                                                            reg.teamMembersDetails.forEach { m ->
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = if (isDark) Color(0xFF232746) else Color(0xFFF1F5F9),
                                                                    border = BorderStroke(0.5.dp, if (isDark) Color(0xFF33385C) else Color(0xFFE2E8F0)),
                                                                    modifier = Modifier.fillMaxWidth()
                                                                ) {
                                                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                        Text("• ${m.name}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF0F172A))
                                                                        if (m.department.isNotBlank()) {
                                                                            Spacer(modifier = Modifier.width(6.dp))
                                                                            Text("• ${m.department} (${m.yearOfStudy})", fontSize = 10.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Markdown Description
                    item {
                        Text(
                            text = "About the Event",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MarkdownText(
                            text = event.description,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                        )
                    }

                    // Rules & Guidelines
                    if (event.rules.isNotEmpty()) {
                        item {
                            Text(
                                text = "Rules & Eligibility",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                event.rules.forEach { rule ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(rule, fontSize = 13.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                    }
                                }
                            }
                        }
                    }

                    // Agenda Timeline
                    if (event.agenda.isNotEmpty()) {
                        item {
                            Text(
                                text = "Event Schedule",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                event.agenda.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isDark) Color(0xFF141629) else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(item.time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                                if (item.description.isNotBlank()) {
                                                    Text(item.description, fontSize = 11.5.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FAQs
                    if (event.faqs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Frequently Asked Questions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                event.faqs.forEach { faq ->
                                    var isExpanded by remember { mutableStateOf(false) }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isDark) Color(0xFF141629) else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { isExpanded = !isExpanded }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = faq.question,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                    contentDescription = null,
                                                    tint = Color(0xFF7C3AED)
                                                )
                                            }
                                            if (isExpanded) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = faq.answer,
                                                    fontSize = 12.sp,
                                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRegistrationForm) {
        EventRegistrationDialog(
            event = event,
            viewModel = viewModel,
            onDismiss = { showRegistrationForm = false },
            onSuccess = {
                showRegistrationForm = false
            }
        )
    }
}

// ─── Event Registration Form Dialog ───

@Composable
private fun EventRegistrationDialog(
    event: EventItem,
    viewModel: EventsViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    // Step state: 1 = Team Info, 2 = Team Members, 3 = Domain, 4 = Project Info, 5 = Review & Submit
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // Registration Mode
    var isTeam by remember {
        mutableStateOf(event.registrationMode != "individual_only")
    }

    // Step 1: Team Info
    var teamName by remember { mutableStateOf("") }
    var teamDescription by remember { mutableStateOf("") }
    var teamGithubUrl by remember { mutableStateOf("") }
    var teamLinkedinUrl by remember { mutableStateOf("") }
    var teamPortfolioUrl by remember { mutableStateOf("") }
    var targetTeamSize by remember {
        mutableIntStateOf(
            if (event.minTeamSize >= 2) event.minTeamSize.coerceAtMost(event.maxTeamSize) else 2.coerceAtMost(event.maxTeamSize)
        )
    }

    // Step 2: Leader Details
    var leaderName by remember { mutableStateOf("") }
    var leaderEmail by remember { mutableStateOf("") }
    var leaderPhone by remember { mutableStateOf("") }
    var leaderCollege by remember { mutableStateOf("") }
    var leaderDept by remember { mutableStateOf("") }
    var leaderYear by remember { mutableStateOf("3rd Year") }

    // Dynamic Teammates List
    val teammates = remember { mutableStateListOf<TeamMemberInfo>() }

    // Synchronize teammates list with targetTeamSize
    LaunchedEffect(targetTeamSize, isTeam) {
        if (isTeam) {
            val neededTeammates = (targetTeamSize - 1).coerceAtLeast(0)
            while (teammates.size < neededTeammates) {
                teammates.add(
                    TeamMemberInfo(
                        name = "",
                        email = "",
                        phone = "",
                        college = leaderCollege,
                        department = leaderDept,
                        yearOfStudy = "3rd Year",
                        role = "Member"
                    )
                )
            }
            while (teammates.size > neededTeammates) {
                teammates.removeAt(teammates.lastIndex)
            }
        } else {
            teammates.clear()
        }
    }

    // Step 3: Domain Selection
    val defaultDomains = listOf(
        "Artificial Intelligence & Machine Learning",
        "Generative AI",
        "Web Development",
        "Mobile App Development",
        "Blockchain & Web3",
        "Cybersecurity",
        "Internet of Things (IoT)",
        "Robotics & Automation",
        "FinTech",
        "Healthcare Technology",
        "Education Technology",
        "Renewable Energy & Sustainability",
        "Agriculture Technology",
        "Open Innovation",
        "Other"
    )
    val availableDomains = remember(event.availableDomains) {
        if (event.availableDomains.isNotEmpty()) event.availableDomains else defaultDomains
    }
    var selectedDomain by remember { mutableStateOf(availableDomains.firstOrNull() ?: "Artificial Intelligence & Machine Learning") }
    var customDomain by remember { mutableStateOf("") }

    // Step 4: Project Information (Optional)
    var projectTitle by remember { mutableStateOf("") }
    var problemStatement by remember { mutableStateOf("") }
    var proposedSolution by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("") }
    var projectGithubUrl by remember { mutableStateOf("") }
    var projectDemoUrl by remember { mutableStateOf("") }

    // Step 5: Confirmations
    var confirmAccurate by remember { mutableStateOf(false) }
    var confirmRules by remember { mutableStateOf(false) }
    var confirmPolicy by remember { mutableStateOf(false) }

    val yearsList = listOf("1st Year", "2nd Year", "3rd Year", "4th Year / Final Year", "Post-Graduate", "Other")

    // Prefill from Firebase Auth
    LaunchedEffect(Unit) {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser != null) {
            if (leaderName.isBlank()) leaderName = authUser.displayName ?: ""
            if (leaderEmail.isBlank()) leaderEmail = authUser.email ?: ""
            if (leaderPhone.isBlank()) leaderPhone = authUser.phoneNumber ?: ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF0B0C16) else Color(0xFFFFFFFF)
        ) {
            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF14162B) else Color(0xFFF8FAFC))
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentStep > 1) currentStep-- else onDismiss()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (currentStep) {
                                        1 -> if (isTeam) "Step 1: Team Information" else "Step 1: Registration Mode"
                                        2 -> if (isTeam) "Step 2: Team Members & Leader" else "Step 2: Participant Details"
                                        3 -> "Step 3: Hackathon Domain"
                                        4 -> "Step 4: Project Concept"
                                        5 -> "Step 5: Review & Submit"
                                        else -> "Event Registration"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = event.title,
                                    fontSize = 11.sp,
                                    color = Color(0xFF7C3AED),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$currentStep of $totalSteps",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Stepper Progress Bar
                        LinearProgressIndicator(
                            progress = { currentStep.toFloat() / totalSteps.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF7C3AED),
                            trackColor = if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)
                        )
                    }
                },
                bottomBar = {
                    Surface(
                        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentStep > 1) {
                                OutlinedButton(
                                    onClick = { currentStep-- },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Previous", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Button(
                                onClick = {
                                    // Validation per step
                                    when (currentStep) {
                                        1 -> {
                                            if (isTeam && teamName.trim().isBlank()) {
                                                Toast.makeText(context, "Please enter your Team Name", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            currentStep = 2
                                        }
                                        2 -> {
                                            if (leaderName.trim().isBlank()) {
                                                Toast.makeText(context, "Please enter Team Leader Full Name", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (leaderEmail.trim().isBlank() || !leaderEmail.contains("@")) {
                                                Toast.makeText(context, "Please enter a valid Team Leader Email", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (leaderPhone.trim().length < 10) {
                                                Toast.makeText(context, "Please enter a valid 10-digit Phone Number for Leader", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (leaderCollege.trim().isBlank()) {
                                                Toast.makeText(context, "Please enter College / Institution for Leader", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (leaderDept.trim().isBlank()) {
                                                Toast.makeText(context, "Please enter Department / Branch for Leader", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }

                                            // Validate all teammates if team
                                            if (isTeam) {
                                                for (i in teammates.indices) {
                                                    val m = teammates[i]
                                                    val num = i + 2
                                                    if (m.name.trim().isBlank()) {
                                                        Toast.makeText(context, "Please enter Full Name for Member $num", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (m.email.trim().isBlank() || !m.email.contains("@")) {
                                                        Toast.makeText(context, "Please enter a valid Email for Member $num", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (m.phone.trim().length < 10) {
                                                        Toast.makeText(context, "Please enter a 10-digit Phone for Member $num", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (m.college.trim().isBlank()) {
                                                        Toast.makeText(context, "Please enter College for Member $num", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (m.department.trim().isBlank()) {
                                                        Toast.makeText(context, "Please enter Department for Member $num", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                }

                                                // Duplicate Email Check
                                                val allEmails = mutableListOf(leaderEmail.trim().lowercase())
                                                allEmails.addAll(teammates.map { it.email.trim().lowercase() })
                                                if (allEmails.toSet().size < allEmails.size) {
                                                    Toast.makeText(context, "Duplicate email detected! Each member must have a unique email address.", Toast.LENGTH_LONG).show()
                                                    return@Button
                                                }
                                            }
                                            currentStep = 3
                                        }
                                        3 -> {
                                            if (selectedDomain == "Other" && customDomain.trim().isBlank()) {
                                                Toast.makeText(context, "Please specify your custom hackathon domain", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            currentStep = 4
                                        }
                                        4 -> {
                                            if (event.requireProjectDetails && projectTitle.trim().isBlank()) {
                                                Toast.makeText(context, "Please enter Project Title", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            currentStep = 5
                                        }
                                        5 -> {
                                            if (!confirmAccurate || !confirmRules || !confirmPolicy) {
                                                Toast.makeText(context, "Please accept all 3 confirmation checkboxes to proceed", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }

                                            // Final Submission
                                            viewModel.submitRegistration(
                                                event = event,
                                                isTeam = isTeam,
                                                teamName = if (isTeam) teamName.trim() else null,
                                                teamCode = null,
                                                userName = leaderName.trim(),
                                                userEmail = leaderEmail.trim(),
                                                userPhone = leaderPhone.trim(),
                                                college = leaderCollege.trim(),
                                                department = leaderDept.trim(),
                                                yearOfStudy = leaderYear.trim(),
                                                skills = "",
                                                githubUrl = teamGithubUrl.trim(),
                                                linkedinUrl = teamLinkedinUrl.trim(),
                                                teamDescription = teamDescription.trim(),
                                                teamGithubUrl = teamGithubUrl.trim(),
                                                teamLinkedinUrl = teamLinkedinUrl.trim(),
                                                teamPortfolioUrl = teamPortfolioUrl.trim(),
                                                teamSize = if (isTeam) (1 + teammates.size) else 1,
                                                domain = if (selectedDomain == "Other") customDomain.trim() else selectedDomain,
                                                customDomain = if (selectedDomain == "Other") customDomain.trim() else "",
                                                projectTitle = projectTitle.trim(),
                                                problemStatement = problemStatement.trim(),
                                                proposedSolution = proposedSolution.trim(),
                                                techStack = techStack.trim(),
                                                projectGithubUrl = projectGithubUrl.trim(),
                                                projectDemoUrl = projectDemoUrl.trim(),
                                                teamMembersDetails = teammates.toList(),
                                                customAnswers = emptyMap(),
                                                onSuccess = {
                                                    Toast.makeText(context, "Team Registration Confirmed! QR Ticket Issued.", Toast.LENGTH_LONG).show()
                                                    onSuccess()
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    }
                                },
                                enabled = !viewModel.isRegistering,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(if (currentStep > 1) 1.5f else 1f)
                                    .height(50.dp)
                            ) {
                                if (viewModel.isRegistering) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Registering Team...", fontWeight = FontWeight.Bold)
                                } else if (currentStep == 5) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit & Generate Pass", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                } else {
                                    Text("Next Step", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (currentStep) {
                        // ──────────────────────────────────────────────
                        // STEP 1: TEAM INFORMATION & MODE
                        // ──────────────────────────────────────────────
                        1 -> {
                            // Mode selection (if event allows both)
                            if (event.registrationMode == "both") {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isDark) Color(0xFF14162B) else Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(4.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isTeam) Color(0xFF7C3AED) else Color.Transparent)
                                                    .clickable { isTeam = true }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "🛡️ Team Registration",
                                                    color = if (isTeam) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (!isTeam) Color(0xFF7C3AED) else Color.Transparent)
                                                    .clickable { isTeam = false }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "👤 Individual Entry",
                                                    color = if (!isTeam) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (isTeam) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "TEAM DETAILS",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF7C3AED),
                                            letterSpacing = 1.sp
                                        )

                                        OutlinedTextField(
                                            value = teamName,
                                            onValueChange = { teamName = it },
                                            label = { Text("Team Name *") },
                                            placeholder = { Text("e.g. Neural Pioneers, CyberKnights") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Text(
                                            text = "Choose a unique, official name for your hackathon team.",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                    }
                                }

                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "TEAM SIZE (TOTAL MEMBERS)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF7C3AED),
                                            letterSpacing = 1.sp
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val minS = event.minTeamSize.coerceAtLeast(2)
                                            val maxS = event.maxTeamSize.coerceAtLeast(minS)
                                            (minS..maxS).forEach { size ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (targetTeamSize == size) Color(0xFF7C3AED) else if (isDark) Color(0xFF1E2138) else Color(0xFFF1F5F9),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { targetTeamSize = size }
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(vertical = 12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "$size",
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 16.sp,
                                                            color = if (targetTeamSize == size) Color.White else if (isDark) Color.White else Color(0xFF0F172A)
                                                        )
                                                        Text(
                                                            text = if (size == 1) "Member" else "Members",
                                                            fontSize = 10.sp,
                                                            color = if (targetTeamSize == size) Color.White.copy(alpha = 0.85f) else Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "OPTIONAL TEAM LINKS & BIO",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp,
                                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            letterSpacing = 1.sp
                                        )

                                        OutlinedTextField(
                                            value = teamDescription,
                                            onValueChange = { teamDescription = it },
                                            label = { Text("Team Motto / Short Bio (Optional)") },
                                            placeholder = { Text("What drives your team?") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            minLines = 2
                                        )

                                        OutlinedTextField(
                                            value = teamGithubUrl,
                                            onValueChange = { teamGithubUrl = it },
                                            label = { Text("Team Organization GitHub (Optional)") },
                                            placeholder = { Text("https://github.com/team-name") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        OutlinedTextField(
                                            value = teamLinkedinUrl,
                                            onValueChange = { teamLinkedinUrl = it },
                                            label = { Text("Team LinkedIn / Website (Optional)") },
                                            placeholder = { Text("https://linkedin.com/company/team") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isDark) Color(0xFF14162B) else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Solo Participant Registration", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                            Text(
                                                "You are registering as an individual participant. On the next step, you will verify your profile details and contact number.",
                                                fontSize = 12.sp,
                                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ──────────────────────────────────────────────
                        // STEP 2: TEAM MEMBERS & LEADER DETAILS
                        // ──────────────────────────────────────────────
                        2 -> {
                            // Leader Card
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF171932) else Color(0xFFFAF5FF),
                                    border = BorderStroke(1.5.dp, Color(0xFF7C3AED).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("👑", fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    if (isTeam) "Team Leader (You)" else "Participant Details",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp,
                                                    color = Color(0xFF7C3AED)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF7C3AED)
                                            ) {
                                                Text("LEADER", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }

                                        OutlinedTextField(
                                            value = leaderName,
                                            onValueChange = { leaderName = it },
                                            label = { Text("Full Name *") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = leaderEmail,
                                            onValueChange = { leaderEmail = it },
                                            label = { Text("Email / Gmail *") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = leaderPhone,
                                            onValueChange = { if (it.length <= 15) leaderPhone = it },
                                            label = { Text("WhatsApp / Phone Number *") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = leaderCollege,
                                            onValueChange = { leaderCollege = it },
                                            label = { Text("College / University / Org *") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = leaderDept,
                                                onValueChange = { leaderDept = it },
                                                label = { Text("Department / Branch *") },
                                                placeholder = { Text("e.g. CSE, IT, ECE") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1.2f),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            // Year of Study dropdown
                                            var yearExpanded by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = leaderYear,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Year") },
                                                    trailingIcon = {
                                                        IconButton(onClick = { yearExpanded = true }) {
                                                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                DropdownMenu(
                                                    expanded = yearExpanded,
                                                    onDismissRequest = { yearExpanded = false }
                                                ) {
                                                    yearsList.forEach { y ->
                                                        DropdownMenuItem(
                                                            text = { Text(y) },
                                                            onClick = {
                                                                leaderYear = y
                                                                yearExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (isTeam && teammates.isNotEmpty() && leaderCollege.isNotBlank()) {
                                            TextButton(
                                                onClick = {
                                                    for (i in teammates.indices) {
                                                        teammates[i] = teammates[i].copy(
                                                            college = leaderCollege,
                                                            department = if (teammates[i].department.isBlank()) leaderDept else teammates[i].department
                                                        )
                                                    }
                                                    Toast.makeText(context, "Applied leader college to all members!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Copy College to all teammates", fontSize = 11.sp, color = Color(0xFF7C3AED))
                                            }
                                        }
                                    }
                                }
                            }

                            // Additional Teammates Cards
                            if (isTeam) {
                                items(teammates.size) { index ->
                                    val memberNum = index + 2
                                    var currentMember = teammates[index]

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("👤", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Member $memberNum", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                                }

                                                if (teammates.size > (event.minTeamSize - 1).coerceAtLeast(1)) {
                                                    IconButton(
                                                        onClick = {
                                                            teammates.removeAt(index)
                                                            targetTeamSize = 1 + teammates.size
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = currentMember.name,
                                                onValueChange = { teammates[index] = currentMember.copy(name = it) },
                                                label = { Text("Full Name *") },
                                                placeholder = { Text("Member $memberNum Name") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = currentMember.email,
                                                onValueChange = { teammates[index] = currentMember.copy(email = it) },
                                                label = { Text("Email Address *") },
                                                placeholder = { Text("unique.email@domain.com") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = currentMember.phone,
                                                onValueChange = { if (it.length <= 15) teammates[index] = currentMember.copy(phone = it) },
                                                label = { Text("Phone / WhatsApp *") },
                                                placeholder = { Text("10-digit number") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = currentMember.college,
                                                onValueChange = { teammates[index] = currentMember.copy(college = it) },
                                                label = { Text("College / Institution *") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = currentMember.department,
                                                    onValueChange = { teammates[index] = currentMember.copy(department = it) },
                                                    label = { Text("Department *") },
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1.2f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )

                                                var mYearExpanded by remember { mutableStateOf(false) }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    OutlinedTextField(
                                                        value = currentMember.yearOfStudy,
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = { Text("Year") },
                                                        trailingIcon = {
                                                            IconButton(onClick = { mYearExpanded = true }) {
                                                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    DropdownMenu(
                                                        expanded = mYearExpanded,
                                                        onDismissRequest = { mYearExpanded = false }
                                                    ) {
                                                        yearsList.forEach { y ->
                                                            DropdownMenuItem(
                                                                text = { Text(y) },
                                                                onClick = {
                                                                    teammates[index] = currentMember.copy(yearOfStudy = y)
                                                                    mYearExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Add Member button if under maxTeamSize
                                if (1 + teammates.size < event.maxTeamSize) {
                                    item {
                                        OutlinedButton(
                                            onClick = {
                                                teammates.add(
                                                    TeamMemberInfo(
                                                        name = "",
                                                        email = "",
                                                        phone = "",
                                                        college = leaderCollege,
                                                        department = leaderDept,
                                                        yearOfStudy = "3rd Year",
                                                        role = "Member"
                                                    )
                                                )
                                                targetTeamSize = 1 + teammates.size
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Add Member ${teammates.size + 2} (${1 + teammates.size + 1}/${event.maxTeamSize})", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // ──────────────────────────────────────────────
                        // STEP 3: HACKATHON DOMAIN SELECTION
                        // ──────────────────────────────────────────────
                        3 -> {
                            item {
                                Text(
                                    text = "SELECT HACKATHON DOMAIN / TRACK",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7C3AED),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Choose the primary domain your team will be building for during the hackathon.",
                                    fontSize = 11.5.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }

                            items(availableDomains.size) { idx ->
                                val domain = availableDomains[idx]
                                val isSelected = selectedDomain == domain
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.12f) else if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF7C3AED) else if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDomain = domain }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = when (domain) {
                                                    "Artificial Intelligence & Machine Learning" -> "🤖"
                                                    "Generative AI" -> "✨"
                                                    "Web Development" -> "🌐"
                                                    "Mobile App Development" -> "📱"
                                                    "Blockchain & Web3" -> "⛓️"
                                                    "Cybersecurity" -> "🛡️"
                                                    "Internet of Things (IoT)" -> "📡"
                                                    "Robotics & Automation" -> "🦾"
                                                    "FinTech" -> "💳"
                                                    "Healthcare Technology" -> "🏥"
                                                    "Education Technology" -> "🎓"
                                                    "Renewable Energy & Sustainability" -> "🌱"
                                                    "Agriculture Technology" -> "🌾"
                                                    "Open Innovation" -> "💡"
                                                    else -> "🎯"
                                                },
                                                fontSize = 20.sp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = domain,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.5.sp,
                                                color = if (isSelected) Color(0xFF7C3AED) else if (isDark) Color.White else Color(0xFF0F172A)
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            if (selectedDomain == "Other") {
                                item {
                                    OutlinedTextField(
                                        value = customDomain,
                                        onValueChange = { customDomain = it },
                                        label = { Text("Enter your preferred hackathon domain *") },
                                        placeholder = { Text("e.g. SpaceTech, Quantum Computing, Defense Tech") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // ──────────────────────────────────────────────
                        // STEP 4: PROJECT AND IDEA INFORMATION
                        // ──────────────────────────────────────────────
                        4 -> {
                            item {
                                Text(
                                    text = "PROJECT & INNOVATION CONCEPT (OPTIONAL)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7C3AED),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You can outline your initial concept now or update it during the hackathon check-in.",
                                    fontSize = 11.5.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = projectTitle,
                                    onValueChange = { projectTitle = it },
                                    label = { Text("Project Title") },
                                    placeholder = { Text("e.g. ZayaSense: Autonomous Healthcare Agent") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = problemStatement,
                                    onValueChange = { problemStatement = it },
                                    label = { Text("Problem Statement") },
                                    placeholder = { Text("What real-world bottleneck are you solving?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    minLines = 2
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = proposedSolution,
                                    onValueChange = { proposedSolution = it },
                                    label = { Text("Proposed Solution & Architecture") },
                                    placeholder = { Text("How does your solution work?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    minLines = 3
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = techStack,
                                    onValueChange = { techStack = it },
                                    label = { Text("Target Tech Stack") },
                                    placeholder = { Text("e.g. Kotlin, Compose, Python, Gemini 1.5, Firestore") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = projectGithubUrl,
                                    onValueChange = { projectGithubUrl = it },
                                    label = { Text("Project GitHub Repository URL (Optional)") },
                                    placeholder = { Text("https://github.com/myteam/project") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = projectDemoUrl,
                                    onValueChange = { projectDemoUrl = it },
                                    label = { Text("Live Demo / Video Pitch URL (Optional)") },
                                    placeholder = { Text("https://youtu.be/... or demo site") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // ──────────────────────────────────────────────
                        // STEP 5: REVIEW & CONFIRMATION
                        // ──────────────────────────────────────────────
                        5 -> {
                            item {
                                Text(
                                    text = "OFFICIAL REGISTRATION REVIEW",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7C3AED),
                                    letterSpacing = 1.sp
                                )
                            }

                            // Card 1: Team & Domain
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isTeam) "🛡️ Team: $teamName" else "👤 Solo Entry",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isDark) Color.White else Color(0xFF0F172A)
                                            )
                                            TextButton(onClick = { currentStep = 1 }) {
                                                Text("Edit", fontSize = 12.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF7C3AED).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (selectedDomain == "Other") customDomain.ifBlank { "Custom Track" } else selectedDomain,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF7C3AED),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }

                                            if (isTeam) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "${1 + teammates.size} Total Members",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF10B981),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Card 2: Team Roster
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "👥 Team Members Roster",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isDark) Color.White else Color(0xFF0F172A)
                                            )
                                            TextButton(onClick = { currentStep = 2 }) {
                                                Text("Edit", fontSize = 12.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Leader
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF7C3AED).copy(alpha = 0.12f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("👑 $leaderName", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7C3AED))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("(Team Leader)", fontSize = 11.sp, color = Color(0xFF7C3AED).copy(alpha = 0.8f))
                                                }
                                                Text("📧 $leaderEmail • 📱 $leaderPhone", fontSize = 11.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                                                Text("🏛️ $leaderCollege • $leaderDept ($leaderYear)", fontSize = 11.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                            }
                                        }

                                        // Teammates
                                        teammates.forEachIndexed { i, m ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isDark) Color(0xFF1E2138) else Color(0xFFF8FAFC),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text("👤 Member ${i + 2}: ${m.name}", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                                    Text("📧 ${m.email} • 📱 ${m.phone}", fontSize = 11.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                                                    Text("🏛️ ${m.college} • ${m.department} (${m.yearOfStudy})", fontSize = 11.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Card 3: Project Info (if entered)
                            if (projectTitle.isNotBlank() || problemStatement.isNotBlank()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("💡 Project Concept", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                                TextButton(onClick = { currentStep = 4 }) {
                                                    Text("Edit", fontSize = 12.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (projectTitle.isNotBlank()) {
                                                Text(projectTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF7C3AED))
                                            }
                                            if (problemStatement.isNotBlank()) {
                                                Text("Problem: $problemStatement", fontSize = 11.5.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            }
                                            if (techStack.isNotBlank()) {
                                                Text("Stack: $techStack", fontSize = 11.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }
                            }

                            // Confirmations
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = confirmAccurate, onCheckedChange = { confirmAccurate = it })
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("I certify that all team member information and contact numbers are accurate.", fontSize = 11.5.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = confirmRules, onCheckedChange = { confirmRules = it })
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Our team agrees to adhere to the official hackathon rules and code of conduct.", fontSize = 11.5.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = confirmPolicy, onCheckedChange = { confirmPolicy = it })
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("We agree to the hackathon terms, conditions, and privacy policy.", fontSize = 11.5.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Digital QR Event Ticket Dialog ───

@Composable
private fun DigitalTicketDialog(
    registration: EventRegistration,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val qrBitmap = remember(registration.qrSecurityToken) {
        QRCodeGenerator.generateQRCodeBitmap(registration.qrSecurityToken, 512)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2E3258) else Color(0xFFE2E8F0)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                Column {
                    // Boarding pass top header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF38BDF8))))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OFFICIAL EVENT PASS",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = registration.registrationId,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = registration.eventTitle,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }

                    // Ticket Body
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PARTICIPANT", fontSize = 10.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(registration.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                            }
                            if (registration.isTeam) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TEAM", fontSize = 10.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Text(registration.teamName ?: "Team ZAYA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF7C3AED))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("COLLEGE / ORG", fontSize = 10.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(registration.college, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155), maxLines = 1)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("STATUS", fontSize = 10.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(if (registration.attended) "CHECKED-IN" else "VALID TICKET", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                            }
                        }

                        val trackDisplay = registration.domain.ifBlank { registration.customDomain }
                        if (trackDisplay.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎯 Track: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF7C3AED))
                                    Text(trackDisplay, fontSize = 11.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (registration.isTeam && registration.teamMembers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1B1E38) else Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("👥 Team Roster (${registration.teamMembers.size} Members):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF7C3AED))
                                    Text(registration.teamMembers.joinToString(" • "), fontSize = 10.5.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // High-contrast QR Code Container
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.size(200.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "Event QR Ticket",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    CircularProgressIndicator(color = Color(0xFF7C3AED), modifier = Modifier.size(32.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Scan this QR code at the event check-in counter",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Close")
                            }

                            Button(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        val trackStr = if (trackDisplay.isNotBlank()) "\nTrack: $trackDisplay" else ""
                                        val teamStr = if (registration.isTeam) "\nTeam: ${registration.teamName ?: "Team"}\nMembers: ${registration.teamMembers.joinToString(", ")}" else ""
                                        val fullText = "🎟️ Official Event Ticket for ${registration.eventTitle}\nTicket ID: ${registration.ticketId}\nParticipant: ${registration.userName}$teamStr$trackStr\nStatus: ${if (registration.attended) "CHECKED-IN ✅" else "CONFIRMED"}"
                                        putExtra(Intent.EXTRA_TEXT, fullText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Event Ticket"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Certificate Viewer Dialog ───

@Composable
private fun CertificateDialog(
    certificate: EventCertificate,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CERTIFICATE OF ACHIEVEMENT",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD97706),
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = certificate.eventTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "This is proudly presented to",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = certificate.participantName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF7C3AED),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "for outstanding performance and completion in the category of ${certificate.type}",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("VERIFICATION ID", fontSize = 9.5.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(certificate.certificateId, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ISSUED ON", fontSize = 9.5.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(dateFormat.format(Date(certificate.issueDate)), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "I just received my official certificate for ${certificate.eventTitle}!\nCredential ID: ${certificate.certificateId}\nVerification link: ${certificate.verificationUrl}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Certificate"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}

// ─── Organizer Attendance Scanner Dialog ───

@Composable
private fun OrganizerScannerDialog(
    viewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var inputToken by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Attendance Scanner", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Scan or type the participant's QR Security Token or Registration ID (e.g. ZAYA-EVT-XXXXX)",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputToken,
                    onValueChange = { inputToken = it },
                    label = { Text("Registration ID / QR Token") },
                    placeholder = { Text("ZAYA-EVT-...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (inputToken.isNotBlank()) {
                            viewModel.verifyTicket(inputToken)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Check In Participant", fontWeight = FontWeight.Bold)
                }

                // Show Scan Result
                viewModel.lastScanResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (result.success) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (result.success) Color(0xFF10B981) else Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.success) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                                contentDescription = null,
                                tint = if (result.success) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (result.success) "SUCCESS" else "SCAN ERROR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (result.success) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Text(result.message, fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── AI Event Assistant Dialog ───

@Composable
private fun EventAiAssistantDialog(
    event: EventItem,
    viewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var questionText by remember { mutableStateOf("") }
    val quickQuestions = listOf(
        "What is the prize pool?",
        "Who is eligible to participate?",
        "What is the team size limit?",
        "What is the schedule / agenda?",
        "Where is the venue located?"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("AI Event Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                            Text("Accurate answers powered by event data", fontSize = 10.5.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = if (isDark) Color(0xFF232544) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                // Answer area or empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (viewModel.isAiAnswering) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF7C3AED), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Analyzing event details...", fontSize = 12.5.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                            }
                        }
                    } else if (viewModel.aiEventAnswer != null) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF1E2138) else Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = "Q: ${viewModel.aiEventQuestion}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF7C3AED),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            item {
                                MarkdownText(
                                    text = viewModel.aiEventAnswer!!,
                                    color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Frequently Asked Quick Questions:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(10.dp))
                            quickQuestions.forEach { q ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDark) Color(0xFF1E2138) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.askAiAboutEvent(event, q) }
                                ) {
                                    Text(
                                        text = q,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF1E2138) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            placeholder = { Text("Ask anything about this event...", fontSize = 12.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (questionText.isNotBlank()) {
                                    viewModel.askAiAboutEvent(event, questionText)
                                    questionText = ""
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Ask", tint = Color(0xFF7C3AED))
                        }
                    }
                }
            }
        }
    }
}

