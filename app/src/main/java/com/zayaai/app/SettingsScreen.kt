package com.zayaai.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.zayaai.app.ui.theme.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    securityViewModel: SecurityViewModel,
    onLogout: () -> Unit,
    onProfileUpdate: () -> Unit,
    paddingValues: PaddingValues
) {
    val settings by securityViewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showEditProfile by remember { mutableStateOf(false) }
    var showLockTypePicker by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    // Local state for name to reflect changes immediately in this screen
    var displayedName by remember(userName) { mutableStateOf(userName) }

    // Check biometric availability
    val biometricManager = remember { BiometricAuthManager(context) }
    val isBiometricAvailable = remember { biometricManager.isBiometricAvailable() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Account Section
        SettingsSection("Account") {
            AccountInfoCard(displayedName, userEmail, onLogout) {
                showEditProfile = true
            }
        }

        // AI Settings
        SettingsSection("AI Settings") {
            SettingsToggleItem(
                title = "Voice Response",
                subtitle = "Speak AI responses automatically",
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                checked = settings.voiceEnabled,
                onCheckedChange = { securityViewModel.setVoiceEnabled(it) }
            )
            SettingsToggleItem(
                title = "AI Typing Animation",
                subtitle = "Show animation while AI is generating",
                icon = Icons.Rounded.TextFormat,
                checked = settings.typingAnimation,
                onCheckedChange = { securityViewModel.setTypingAnimation(it) }
            )
        }

        // Appearance
        SettingsSection("Appearance") {
            ThemeModeSelector(
                currentMode = settings.themeMode,
                onModeSelected = { securityViewModel.setThemeMode(it) }
            )
        }

        // Security
        SettingsSection("Security") {
            SettingsToggleItem(
                title = "App Lock",
                subtitle = "Require authentication to enter",
                icon = Icons.Rounded.Security,
                checked = settings.appLockEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && settings.pinCode.isEmpty()) {
                        // Prompt user to set PIN first
                        showSetPinDialog = true
                    } else {
                        securityViewModel.updateAppLock(enabled)
                    }
                }
            )
            if (settings.appLockEnabled) {
                SettingsClickItem(
                    title = "Lock Type",
                    subtitle = "Current: ${settings.lockType}",
                    icon = Icons.Rounded.Lock,
                    onClick = { showLockTypePicker = true }
                )
                SettingsClickItem(
                    title = "Change PIN",
                    subtitle = "Update your security PIN",
                    icon = Icons.Rounded.Pin,
                    onClick = { showSetPinDialog = true }
                )
            }
            if (isBiometricAvailable) {
                SettingsClickItem(
                    title = "Fingerprint",
                    subtitle = if (settings.lockType.contains("Fingerprint")) "Enabled" else "Tap to configure in Lock Type",
                    icon = Icons.Rounded.Fingerprint,
                    color = if (settings.lockType.contains("Fingerprint")) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { showLockTypePicker = true }
                )
            }
        }

        // Notifications
        SettingsSection("Notifications") {
            SettingsToggleItem(
                title = "Push Notifications",
                subtitle = "Receive study reminders & updates",
                icon = Icons.Rounded.Notifications,
                checked = settings.notificationsEnabled,
                onCheckedChange = { securityViewModel.setNotificationsEnabled(it) }
            )
            SettingsToggleItem(
                title = "Study Reminders",
                subtitle = "Daily reminders to keep studying",
                icon = Icons.Rounded.Alarm,
                checked = settings.studyReminders,
                onCheckedChange = { securityViewModel.setStudyReminders(it) }
            )
        }

        // Data
        SettingsSection("Data") {
            SettingsClickItem(
                title = "Clear Chat History",
                subtitle = "Remove all local and cloud messages",
                icon = Icons.Rounded.DeleteSweep,
                color = Color(0xFFFF5252),
                onClick = { showClearHistoryConfirm = true }
            )
            SettingsClickItem(
                title = "Clear AI Cache",
                subtitle = "Remove cached AI responses",
                icon = Icons.Rounded.ClearAll,
                color = Color(0xFFFF9800),
                onClick = {
                    scope.launch {
                        securityViewModel.clearAiCache()
                        Toast.makeText(context, "AI cache cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // About
        SettingsSection("About") {
            AboutCard()
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    // --- Dialogs ---

    if (showEditProfile) {
        EditProfileDialog(
            currentName = displayedName,
            onDismiss = { showEditProfile = false },
            onSave = { newName ->
                scope.launch {
                    try {
                        val user = Firebase.auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = newName
                        }
                        user?.updateProfile(profileUpdates)?.await()
                        user?.reload()?.await()
                        displayedName = newName
                        onProfileUpdate()
                        showEditProfile = false
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        )
    }

    if (showLockTypePicker) {
        LockTypePickerDialog(
            currentType = settings.lockType,
            isBiometricAvailable = isBiometricAvailable,
            onSelect = { type ->
                securityViewModel.setLockType(type)
                showLockTypePicker = false
            },
            onDismiss = { showLockTypePicker = false }
        )
    }

    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onSave = { newPin ->
                securityViewModel.setPinCode(newPin)
                if (!settings.appLockEnabled) {
                    securityViewModel.updateAppLock(true)
                }
                showSetPinDialog = false
                Toast.makeText(context, "PIN saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear Chat History", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("Are you sure? This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            securityViewModel.clearChatHistory()
                            showClearHistoryConfirm = false
                            Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// â”€â”€â”€ Theme Mode Selector â”€â”€â”€

@Composable
fun ThemeModeSelector(currentMode: String, onModeSelected: (String) -> Unit) {
    val modes = listOf(
        Triple("light", "Light", Icons.Rounded.LightMode),
        Triple("dark", "Dark", Icons.Rounded.DarkMode),
        Triple("system", "System", Icons.Rounded.SettingsBrightness)
    )
    Column(modifier = Modifier.padding(12.dp)) {
        modes.forEach { (value, label, icon) ->
            val isSelected = currentMode == value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else Color.Transparent
                    )
                    .clickable { onModeSelected(value) }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon, null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                RadioButton(
                    selected = isSelected,
                    onClick = { onModeSelected(value) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

// â”€â”€â”€ Dialogs â”€â”€â”€

@Composable
fun LockTypePickerDialog(
    currentType: String,
    isBiometricAvailable: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = mutableListOf("PIN")
    if (isBiometricAvailable) {
        options.add("Fingerprint")
        options.add("PIN + Fingerprint")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Lock Type", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(option) }
                            .background(
                                if (option == currentType) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentType,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(option, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            Text(
                                when (option) {
                                    "PIN" -> "Unlock with numeric PIN"
                                    "Fingerprint" -> "Unlock with fingerprint only"
                                    "PIN + Fingerprint" -> "Use fingerprint first, PIN as fallback"
                                    else -> ""
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SetPinDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set PIN", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a 4-6 digit PIN", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall)
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

// â”€â”€â”€ Reusable Settings Components â”€â”€â”€

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        ) {
            content()
        }
    }
}

@Composable
fun AccountInfoCard(userName: String, userEmail: String, onLogout: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "U",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName.ifEmpty { "User" },
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = userEmail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Tap to edit profile",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = "Logout",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Display Name", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Update", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsClickItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AboutCard() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("AI ZAYA", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Version 1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Developed by Zaya Code Hub", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

