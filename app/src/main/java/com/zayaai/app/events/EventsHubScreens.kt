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

    
    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
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
                            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Color(0xFF7C3AED))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Announcements", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        IconButton(onClick = { showNotificationDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Official updates, deadline reminders, and schedule changes from organizers will appear here in real-time.",
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showNotificationDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Got it")
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

    var isTeam by remember { mutableStateOf(event.maxTeamSize > 1) }
    var teamName by remember { mutableStateOf("") }
    var teamCode by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var yearOfStudy by remember { mutableStateOf("3rd Year") }
    var skills by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var linkedinUrl by remember { mutableStateOf("") }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Event Registration", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                            Text(event.title, fontSize = 11.5.sp, color = Color(0xFF7C3AED), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                },
                bottomBar = {
                    Surface(
                        color = if (isDark) Color(0xFF14162B) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF282B48) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (college.isBlank()) {
                                    Toast.makeText(context, "Please enter your college or institute", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.submitRegistration(
                                    event = event,
                                    isTeam = isTeam,
                                    teamName = teamName,
                                    teamCode = teamCode.ifBlank { null },
                                    college = college,
                                    department = department,
                                    yearOfStudy = yearOfStudy,
                                    skills = skills,
                                    githubUrl = githubUrl,
                                    linkedinUrl = linkedinUrl,
                                    customAnswers = emptyMap(),
                                    onSuccess = {
                                        Toast.makeText(context, "Registration Confirmed! Ticket Generated.", Toast.LENGTH_LONG).show()
                                        onSuccess()
                                    }
                                )
                            },
                            enabled = !viewModel.isRegistering,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (viewModel.isRegistering) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm & Generate QR Ticket", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Team vs Individual selector
                    if (event.maxTeamSize > 1) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF14162B) else Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (!isTeam) Color(0xFF7C3AED) else Color.Transparent)
                                            .clickable { isTeam = false }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Individual Entry", color = if (!isTeam) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isTeam) Color(0xFF7C3AED) else Color.Transparent)
                                            .clickable { isTeam = true }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Team (${event.minTeamSize}-${event.maxTeamSize} Members)", color = if (isTeam) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (isTeam) {
                        item {
                            OutlinedTextField(
                                value = teamName,
                                onValueChange = { teamName = it },
                                label = { Text("Team Name (Optional)") },
                                placeholder = { Text("e.g. Code Wizards") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = teamCode,
                                onValueChange = { teamCode = it },
                                label = { Text("Have a Team Code? (Optional)") },
                                placeholder = { Text("Enter 6-char code to join existing team") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = college,
                            onValueChange = { college = it },
                            label = { Text("College / University / Organization *") },
                            placeholder = { Text("e.g. Indian Institute of Technology") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department / Branch") },
                            placeholder = { Text("e.g. Computer Science, IT, Electronics") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = skills,
                            onValueChange = { skills = it },
                            label = { Text("Primary Skills & Stack") },
                            placeholder = { Text("e.g. Android, Flutter, Python, GenAI") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = githubUrl,
                            onValueChange = { githubUrl = it },
                            label = { Text("GitHub / Portfolio Profile URL") },
                            placeholder = { Text("https://github.com/username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = linkedinUrl,
                            onValueChange = { linkedinUrl = it },
                            label = { Text("LinkedIn Profile URL") },
                            placeholder = { Text("https://linkedin.com/in/username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                        putExtra(Intent.EXTRA_TEXT, "Here is my official ticket for ${registration.eventTitle}!\nRegistration ID: ${registration.registrationId}\nParticipant: ${registration.userName}")
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

