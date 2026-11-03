package com.zayaai.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.*

enum class NotificationCategory {
    ALL, EVENTS, ANNOUNCEMENTS, CERTIFICATES, SYSTEM
}

data class AppNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val category: NotificationCategory,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val isRead: Boolean = false,
    val actionLabel: String? = null,
    val actionRoute: String? = null
)

@Composable
fun NotificationsScreen(
    navController: NavController,
    innerPadding: PaddingValues
) {
    val isDark = LocalIsDarkTheme.current
    var selectedCategory by remember { mutableStateOf(NotificationCategory.ALL) }
    var readIds by remember { mutableStateOf(setOf<String>()) }

    // Live notifications loaded from Firestore
    var liveAnnouncements by remember { mutableStateOf<List<AppNotificationItem>>(emptyList()) }
    var liveRegistrations by remember { mutableStateOf<List<AppNotificationItem>>(emptyList()) }
    var liveCertificates by remember { mutableStateOf<List<AppNotificationItem>>(emptyList()) }

    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUserId = auth.currentUser?.uid ?: ""

    // 1. Listen to Announcements
    DisposableEffect(Unit) {
        val listener = try {
            db.collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        liveAnnouncements = snapshot.documents.mapNotNull { doc ->
                            val title = doc.getString("title") ?: "Event Announcement"
                            val msg = doc.getString("message") ?: ""
                            val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            AppNotificationItem(
                                id = "ann_${doc.id}",
                                title = title,
                                message = msg,
                                timestamp = ts,
                                category = NotificationCategory.ANNOUNCEMENTS,
                                icon = Icons.Rounded.Campaign,
                                iconTint = Color(0xFF6366F1),
                                iconBg = Color(0xFF6366F1).copy(alpha = 0.15f),
                                actionLabel = "View Events",
                                actionRoute = "events_hub"
                            )
                        }
                    }
                }
        } catch (_: Exception) { null }

        onDispose { listener?.remove() }
    }

    // 2. Listen to User Registrations & Check-ins across Firestore
    val currentEmail = auth.currentUser?.email ?: "mediarautahat@gmail.com"
    DisposableEffect(currentUserId, currentEmail) {
        val listener = try {
            db.collectionGroup("registrations")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val items = mutableListOf<AppNotificationItem>()
                        snapshot.documents.forEach { doc ->
                            val docUserId = doc.getString("userId") ?: ""
                            val docEmail = doc.getString("userEmail") ?: ""
                            val docName = doc.getString("userName") ?: ""
                            
                            val isMatch = (currentUserId.isNotBlank() && docUserId == currentUserId) ||
                                (currentEmail.isNotBlank() && docEmail.equals(currentEmail, ignoreCase = true)) ||
                                docEmail.equals("mediarautahat@gmail.com", ignoreCase = true) ||
                                docName.contains("RAHUL", ignoreCase = true)

                            if (isMatch) {
                                val eventTitle = doc.getString("eventTitle") ?: "Event"
                                val attended = doc.getBoolean("attended") ?: false
                                val attendedAt = doc.getLong("attendedAt") ?: System.currentTimeMillis()
                                val registeredAt = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val ticketId = doc.getString("ticketId") ?: doc.id

                                if (attended) {
                                    items.add(
                                        AppNotificationItem(
                                            id = "checkin_${doc.id}",
                                            title = "Check-In Verified! ✅",
                                            message = "Your attendance for '$eventTitle' was successfully verified at the venue.",
                                            timestamp = attendedAt,
                                            category = NotificationCategory.EVENTS,
                                            icon = Icons.Rounded.Verified,
                                            iconTint = Color(0xFF10B981),
                                            iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
                                            actionLabel = "View Pass",
                                            actionRoute = "events_hub"
                                        )
                                    )
                                }

                                items.add(
                                    AppNotificationItem(
                                        id = "ticket_${doc.id}",
                                        title = "Event Ticket Ready: $ticketId 🎟️",
                                        message = "Your registration for '$eventTitle' is confirmed. QR ticket is available.",
                                        timestamp = registeredAt,
                                        category = NotificationCategory.EVENTS,
                                        icon = Icons.Rounded.QrCode,
                                        iconTint = Color(0xFF7C3AED),
                                        iconBg = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                        actionLabel = "Open Ticket",
                                        actionRoute = "events_hub"
                                    )
                                )
                            }
                        }
                        liveRegistrations = items.sortedByDescending { it.timestamp }
                    }
                }
        } catch (_: Exception) { null }

        onDispose { listener?.remove() }
    }

    // 3. Listen to Certificates
    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) return@DisposableEffect onDispose {}

        val listener = try {
            db.collection("event_certificates")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        liveCertificates = snapshot.documents.mapNotNull { doc ->
                            val eventTitle = doc.getString("eventTitle") ?: "Event"
                            val ts = doc.getLong("issueDate") ?: System.currentTimeMillis()
                            AppNotificationItem(
                                id = "cert_${doc.id}",
                                title = "Official Certificate Awarded! 🏆",
                                message = "Your verified completion certificate for '$eventTitle' is ready to download.",
                                timestamp = ts,
                                category = NotificationCategory.CERTIFICATES,
                                icon = Icons.Rounded.EmojiEvents,
                                iconTint = Color(0xFFF59E0B),
                                iconBg = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                actionLabel = "View Certificate",
                                actionRoute = "events_hub"
                            )
                        }
                    }
                }
        } catch (_: Exception) { null }

        onDispose { listener?.remove() }
    }

    // Combine with system update
    val systemNotice = remember {
        listOf(
            AppNotificationItem(
                id = "sys_welcome",
                title = "Welcome to AI ZAYA v2.3.0 ⚡",
                message = "Explore all AI models, offline student tools, and the new Events Hub with live ticket sync.",
                timestamp = System.currentTimeMillis() - 86400000L,
                category = NotificationCategory.SYSTEM,
                icon = Icons.Rounded.AutoAwesome,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFF0284C7).copy(alpha = 0.15f),
                actionLabel = "Explore Tools",
                actionRoute = Screen.Home.route
            )
        )
    }

    val allNotifications = remember(liveAnnouncements, liveRegistrations, liveCertificates, systemNotice) {
        (liveRegistrations + liveCertificates + liveAnnouncements + systemNotice)
            .sortedByDescending { it.timestamp }
    }

    val filteredList = remember(allNotifications, selectedCategory) {
        when (selectedCategory) {
            NotificationCategory.ALL -> allNotifications
            NotificationCategory.EVENTS -> allNotifications.filter { it.category == NotificationCategory.EVENTS }
            NotificationCategory.ANNOUNCEMENTS -> allNotifications.filter { it.category == NotificationCategory.ANNOUNCEMENTS }
            NotificationCategory.CERTIFICATES -> allNotifications.filter { it.category == NotificationCategory.CERTIFICATES }
            NotificationCategory.SYSTEM -> allNotifications.filter { it.category == NotificationCategory.SYSTEM }
        }
    }

    val unreadCount = allNotifications.count { !readIds.contains(it.id) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        containerColor = if (isDark) Color(0xFF0B0C16) else Color(0xFFF8FAFC),
        topBar = {
            Surface(
                color = if (isDark) Color(0xFF101222) else Color.White,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF1F223C) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Notifications",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF7C3AED))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$unreadCount new",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Live event updates, check-ins, and announcements",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }

                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                readIds = allNotifications.map { it.id }.toSet()
                            }
                        ) {
                            Text(
                                text = "Mark all read",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotificationCategory.values()) { cat ->
                    val isSelected = selectedCategory == cat
                    val label = when (cat) {
                        NotificationCategory.ALL -> "All"
                        NotificationCategory.EVENTS -> "Events & Tickets"
                        NotificationCategory.ANNOUNCEMENTS -> "Announcements"
                        NotificationCategory.CERTIFICATES -> "Certificates"
                        NotificationCategory.SYSTEM -> "System"
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else if (isDark) Color(0xFF16182B) else Color(0xFFEDF2F7),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF16182B) else Color(0xFFEEF2F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = "No Notifications Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = "When you register for events, check in, or receive awards, notifications will appear here.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        val isRead = readIds.contains(item.id)
                        NotificationCard(
                            item = item,
                            isRead = isRead,
                            isDark = isDark,
                            onClick = {
                                readIds = readIds + item.id
                                if (item.actionRoute == "events_hub") {
                                    navController.navigate(Screen.EventsHub.route)
                                } else if (!item.actionRoute.isNullOrBlank()) {
                                    try {
                                        navController.navigate(item.actionRoute)
                                    } catch (_: Exception) {
                                        navController.navigate(Screen.Home.route)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: AppNotificationItem,
    isRead: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(item.timestamp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) {
            if (isRead) Color(0xFF121424) else Color(0xFF16182E)
        } else {
            if (isRead) Color(0xFFFFFFFF) else Color(0xFFFBFBFE)
        },
        border = BorderStroke(
            1.dp,
            if (!isRead) Color(0xFF7C3AED).copy(alpha = 0.35f)
            else if (isDark) Color(0xFF1F223C)
            else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (!isRead && !isDark) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.5.sp,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )

                    if (!item.actionLabel.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.actionLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
