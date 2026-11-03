package com.zayaai.app
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zayaai.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    securityViewModel: SecurityViewModel,
    onLogout: () -> Unit,
    onProfileUpdate: () -> Unit,
    paddingValues: PaddingValues,
    navController: NavController? = null
) {
    val settings by securityViewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Dialog States
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLockTypePicker by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Notifications Granular State
    var eventAnnouncements by remember { mutableStateOf(true) }
    var eventRegistrations by remember { mutableStateOf(true) }
    var learningReminders by remember { mutableStateOf(settings.studyReminders) }
    var appAnnouncements by remember { mutableStateOf(true) }

    // Selected Language State
    var selectedLanguage by remember { mutableStateOf("English (US)") }

    // Real Cache Calculation
    var cacheSizeMb by remember { mutableStateOf(calculateCacheSize(context)) }

    // Check biometric availability
    val biometricManager = remember { BiometricAuthManager(context) }
    val isBiometricAvailable = remember { biometricManager.isBiometricAvailable() }

    val cleanName = if (userName.isNotBlank() && !userName.equals("User", ignoreCase = true)) userName
    else currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "AI Explorer"
    val cleanEmail = if (userEmail.isNotBlank()) userEmail else currentUser?.email ?: "zayacodehub@gmail.com"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings & Preferences",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ─── 1. APPEARANCE CATEGORY ───
            SettingsCategoryHeader(title = "Appearance", icon = Icons.Rounded.Palette)
            SettingsCard(isDark = isDark) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose light, dark, or follow device system settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ThemeModeSelector(
                        currentMode = settings.themeMode,
                        onModeSelected = { securityViewModel.setThemeMode(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme Preview Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF1E2038) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E3258) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDark) "Dark Theme Active" else "Light Theme Active",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Consistent typography & high-contrast surfaces",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── 2. ACCOUNT & SECURITY CATEGORY ───
            SettingsCategoryHeader(title = "Account & Security", icon = Icons.Rounded.Security)
            SettingsCard(isDark = isDark) {
                // Account Summary Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate(Screen.Profile.route)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = cleanName.firstOrNull()?.uppercaseChar() ?: 'Z'
                        Text(
                            text = initial.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = cleanEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = PrimaryPurple.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "View",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryPurple
                        )
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                // Action: Edit Profile
                SettingsActionItem(
                    title = "Edit Profile",
                    subtitle = "Update name, photo, college, department & bio",
                    icon = Icons.Rounded.Edit,
                    onClick = {
                        navController?.navigate(Screen.EditProfile.route)
                    }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                // Action: Change Password
                SettingsActionItem(
                    title = "Change Password",
                    subtitle = "Send official reset email to verify & change password",
                    icon = Icons.Rounded.LockReset,
                    onClick = { showPasswordResetDialog = true }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                // App Lock Toggle
                SettingsToggleItem(
                    title = "App Lock Protection",
                    subtitle = "Secure app with PIN or biometric authentication",
                    icon = Icons.Rounded.Lock,
                    checked = settings.appLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && settings.pinCode.isEmpty() && settings.patternCode.isEmpty()) {
                            showSetPinDialog = true
                        } else {
                            securityViewModel.updateAppLock(enabled)
                        }
                    }
                )

                if (settings.appLockEnabled) {
                    HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                    SettingsActionItem(
                        title = "Lock Type",
                        subtitle = "Currently: ${settings.lockType}",
                        icon = Icons.Rounded.Fingerprint,
                        onClick = { showLockTypePicker = true }
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                    SettingsActionItem(
                        title = "Change PIN Code",
                        subtitle = "Update your 4-6 digit security PIN",
                        icon = Icons.Rounded.Pin,
                        onClick = { showSetPinDialog = true }
                    )
                }

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                // Logout
                SettingsActionItem(
                    title = "Log Out",
                    subtitle = "Sign out from your active account on this device",
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    tint = Color(0xFFEF4444),
                    onClick = { showLogoutDialog = true }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                // Delete Account
                SettingsActionItem(
                    title = "Delete Account",
                    subtitle = "Permanently remove your account and user profile data",
                    icon = Icons.Rounded.DeleteForever,
                    tint = Color(0xFFDC2626),
                    onClick = { showDeleteAccountDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── 3. NOTIFICATIONS CATEGORY ───
            SettingsCategoryHeader(title = "Notifications", icon = Icons.Rounded.Notifications)
            SettingsCard(isDark = isDark) {
                SettingsToggleItem(
                    title = "Event Announcements",
                    subtitle = "Hackathon, workshop, and webinar notices",
                    icon = Icons.Rounded.Campaign,
                    checked = eventAnnouncements,
                    onCheckedChange = { eventAnnouncements = it }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsToggleItem(
                    title = "Registration & Team Updates",
                    subtitle = "Ticket confirmations, team rosters & check-in alerts",
                    icon = Icons.Rounded.ConfirmationNumber,
                    checked = eventRegistrations,
                    onCheckedChange = { eventRegistrations = it }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsToggleItem(
                    title = "AI Learning Reminders",
                    subtitle = "Daily study tips, quiz challenges & flashcards",
                    icon = Icons.Rounded.Alarm,
                    checked = learningReminders,
                    onCheckedChange = {
                        learningReminders = it
                        securityViewModel.setStudyReminders(it)
                    }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsToggleItem(
                    title = "AI ZAYA App Updates",
                    subtitle = "New multimodal tools and feature announcements",
                    icon = Icons.Rounded.NewReleases,
                    checked = appAnnouncements,
                    onCheckedChange = { appAnnouncements = it }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsActionItem(
                    title = "System Notification Settings",
                    subtitle = "Configure system channels and lockscreen visibility in Android",
                    icon = Icons.Rounded.SettingsApplications,
                    onClick = {
                        openAppNotificationSettings(context)
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── 4. LANGUAGE & REGION CATEGORY ───
            SettingsCategoryHeader(title = "Language & Region", icon = Icons.Rounded.Language)
            SettingsCard(isDark = isDark) {
                SettingsActionItem(
                    title = "App Language",
                    subtitle = selectedLanguage,
                    icon = Icons.Rounded.Translate,
                    onClick = { showLanguageDialog = true }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                // Local format preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Date & Time Formatting",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val formattedDate = remember {
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date())
                        }
                        Text(
                            text = "Device Locale: $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── 5. PRIVACY & PERMISSIONS CATEGORY ───
            SettingsCategoryHeader(title = "Privacy & Permissions", icon = Icons.Rounded.PrivacyTip)
            SettingsCard(isDark = isDark) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Permission Status",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission Rows
                    PermissionStatusRow(
                        name = "Microphone (Voice Assistant)",
                        isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        name = "Camera (Document & QR Scanner)",
                        isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        name = "Storage & Photos (Profile & Poster)",
                        isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                        } else {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        name = "Push Notifications",
                        isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else true
                    )
                }

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                SettingsActionItem(
                    title = "Open Android App Settings",
                    subtitle = "Grant or revoke hardware permissions in Android OS",
                    icon = Icons.Rounded.OpenInNew,
                    onClick = { openAppSystemSettings(context) }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                SettingsActionItem(
                    title = "Privacy Policy",
                    subtitle = "How AI ZAYA handles chat data and account security",
                    icon = Icons.Rounded.Policy,
                    onClick = { showPrivacyPolicyDialog = true }
                )

                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))

                SettingsActionItem(
                    title = "Terms of Service",
                    subtitle = "Acceptable use guidelines for AI tools and Events Hub",
                    icon = Icons.Rounded.Gavel,
                    onClick = { showTermsDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── 6. APP PREFERENCES CATEGORY ───
            SettingsCategoryHeader(title = "App Preferences", icon = Icons.Rounded.Tune)
            SettingsCard(isDark = isDark) {
                SettingsToggleItem(
                    title = "Voice Speech Output",
                    subtitle = "Speak AI assistant responses automatically with TTS",
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    checked = settings.voiceEnabled,
                    onCheckedChange = { securityViewModel.setVoiceEnabled(it) }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsToggleItem(
                    title = "AI Typing Animation",
                    subtitle = "Show live stream effect while AI is generating responses",
                    icon = Icons.Rounded.TextFormat,
                    checked = settings.typingAnimation,
                    onCheckedChange = { securityViewModel.setTypingAnimation(it) }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsActionItem(
                    title = "Clear Cache & Temporary Files",
                    subtitle = "$cacheSizeMb of cached images, models and temp data",
                    icon = Icons.Rounded.CleaningServices,
                    onClick = {
                        val cleared = clearAppCache(context)
                        cacheSizeMb = calculateCacheSize(context)
                        Toast.makeText(context, "Cleared $cleared of cache data!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── 7. HELP & SUPPORT CATEGORY ───
            SettingsCategoryHeader(title = "Help & Support", icon = Icons.Rounded.HelpOutline)
            SettingsCard(isDark = isDark) {
                SettingsActionItem(
                    title = "Contact Support",
                    subtitle = "Email zayacodehub@gmail.com for help and queries",
                    icon = Icons.Rounded.Mail,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:zayacodehub@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "AI ZAYA Support Request - $cleanName")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                SettingsActionItem(
                    title = "Report a Problem",
                    subtitle = "Send diagnostics and issue reports to developers",
                    icon = Icons.Rounded.BugReport,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:zayacodehub@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Bug Report: AI ZAYA v2.4.0")
                            putExtra(Intent.EXTRA_TEXT, "Device: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}\nUser: $cleanEmail\n\nDescribe the issue here:\n")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "AI ZAYA App Version",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "v2.4.0 (Build 240) • Developed by ZAYA CODE HUB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ─── DIALOGS ───

    // Change Password Dialog (Official Firebase sendPasswordResetEmail)
    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            icon = { Icon(Icons.Rounded.LockReset, contentDescription = null, tint = PrimaryPurple) },
            title = { Text("Reset Password") },
            text = {
                Text(
                    "We will send an official password reset link to:\n\n$cleanEmail\n\nPlease click the link in your email to securely choose a new password."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordResetDialog = false
                        auth.sendPasswordResetEmail(cleanEmail)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Password reset email sent to $cleanEmail", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to send reset email: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Send Reset Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("Log Out?") },
            text = { Text("Are you sure you want to log out of AI ZAYA? You will need to sign in again to access your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Delete Account Permanently") },
            text = {
                Text(
                    "CAUTION: This will permanently delete your AI ZAYA account, registered events, certificates, and chat history. This action CANNOT be undone.\n\nAre you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        scope.launch {
                            try {
                                val uid = currentUser?.uid
                                if (uid != null) {
                                    withContext(Dispatchers.IO) {
                                        FirebaseFirestore.getInstance().collection("users").document(uid).delete().await()
                                    }
                                }
                                currentUser?.delete()?.await()
                                Toast.makeText(context, "Your account has been deleted.", Toast.LENGTH_LONG).show()
                                onLogout()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Delete failed: ${e.localizedMessage}. Please re-authenticate first.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Yes, Delete My Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        val languages = listOf(
            "English (US)",
            "English (India)",
            "Hindi (हिन्दी)",
            "Tamil (தமிழ்)",
            "Telugu (తెలుగు)",
            "Spanish (Español)",
            "French (Français)"
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Application Language") },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = lang == selectedLanguage,
                                onClick = {
                                    selectedLanguage = lang
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(lang, fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // PIN Setup Dialog
    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onSave = { pin ->
                securityViewModel.setPinCode(pin)
                securityViewModel.setLockType("PIN")
                securityViewModel.updateAppLock(true)
                showSetPinDialog = false
                Toast.makeText(context, "PIN code configured successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Lock Type Picker
    if (showLockTypePicker) {
        AlertDialog(
            onDismissRequest = { showLockTypePicker = false },
            title = { Text("Choose Lock Type") },
            text = {
                Column {
                    listOf("PIN", "Biometric").forEach { type ->
                        val available = type != "Biometric" || isBiometricAvailable
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = available) {
                                    securityViewModel.setLockType(type)
                                    showLockTypePicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.lockType == type,
                                onClick = {
                                    if (available) {
                                        securityViewModel.setLockType(type)
                                        showLockTypePicker = false
                                    }
                                },
                                enabled = available
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (type == "Biometric" && !isBiometricAvailable) "Biometric (Not available on device)" else type,
                                color = if (available) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLockTypePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "AI ZAYA Privacy & Data Protection\n\n" +
                        "1. Authentication & Credentials:\n" +
                        "AI ZAYA utilizes Google Firebase Auth for safe, encrypted authentication. Your passwords are never stored in plain text.\n\n" +
                        "2. AI Chat & Queries:\n" +
                        "Conversations with AI ZAYA are encrypted in transit and processed using Google Gemini multimodal AI. Chat histories are retained securely for your reference and can be cleared at any time in App Preferences.\n\n" +
                        "3. Hackathons & Events Data:\n" +
                        "Event registrations, team rosters, and certificate verifications are synchronized via Cloud Firestore rules enforcing ownership and administrative access checks.\n\n" +
                        "4. Device Permissions:\n" +
                        "Microphone access is used solely for voice assistant interaction with your active initiation. Camera access is used solely for document analysis and QR attendance scanning.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "AI ZAYA Terms of Service\n\n" +
                        "1. Acceptable Use:\n" +
                        "Users agree to use AI ZAYA for legitimate educational, productivity, and hackathon participation purposes.\n\n" +
                        "2. Team & Event Registrations:\n" +
                        "All details submitted during hackathon and event registrations must be truthful and accurate. Misrepresentation of team members is prohibited.\n\n" +
                        "3. AI Generation Content:\n" +
                        "AI ZAYA assists in study planning, coding, and document analysis. Users should verify technical solutions before submitting for academic evaluations.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

// ── Helper Functions ──

private fun calculateCacheSize(context: Context): String {
    return try {
        var size = getDirSize(context.cacheDir)
        context.externalCacheDir?.let { size += getDirSize(it) }
        val mb = size / (1024.0 * 1024.0)
        String.format(Locale.getDefault(), "%.1f MB", mb)
    } catch (e: Exception) {
        "0.0 MB"
    }
}

private fun getDirSize(dir: File?): Long {
    if (dir == null || !dir.exists()) return 0L
    var size = 0L
    dir.listFiles()?.forEach { file ->
        size += if (file.isDirectory) getDirSize(file) else file.length()
    }
    return size
}

private fun clearAppCache(context: Context): String {
    val initialSize = calculateCacheSize(context)
    try {
        context.cacheDir?.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    } catch (_: Exception) {}
    return initialSize
}

private fun openAppSystemSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun openAppNotificationSettings(context: Context) {
    try {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

// ─── Sub-Composables ───

@Composable
private fun SettingsCategoryHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryPurple,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            ),
            color = PrimaryPurple
        )
    }
}

@Composable
private fun SettingsCard(
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF16172A) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF242646) else Color(0xFFE2E8F0))
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = tint ?: MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryPurple
            )
        )
    }
}

@Composable
private fun PermissionStatusRow(name: String, isGranted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
        ) {
            Text(
                text = if (isGranted) "Granted" else "Not Granted",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

@Composable
private fun ThemeModeSelector(currentMode: String, onModeSelected: (String) -> Unit) {
    val modes = listOf(
        Triple("light", "Light", Icons.Rounded.LightMode),
        Triple("dark", "Dark", Icons.Rounded.DarkMode),
        Triple("system", "System", Icons.Rounded.BrightnessAuto)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        modes.forEach { (mode, label, icon) ->
            val isSelected = currentMode.equals(mode, ignoreCase = true)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onModeSelected(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SetPinDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Security PIN") },
        text = {
            Column {
                Text(
                    "Enter a 4-6 digit numeric PIN to protect your app sessions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPin.length < 4 -> error = "PIN must be at least 4 digits"
                        newPin != confirmPin -> error = "PINs do not match"
                        else -> onSave(newPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Save PIN", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
