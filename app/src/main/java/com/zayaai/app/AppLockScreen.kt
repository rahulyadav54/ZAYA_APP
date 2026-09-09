package com.zayaai.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.zayaai.app.ui.theme.*

@Composable
fun AppLockScreen(
    securityViewModel: SecurityViewModel,
    onUnlock: () -> Unit
) {
    val settings by securityViewModel.settings.collectAsState()
    val context = LocalContext.current
    val biometricManager = remember { BiometricAuthManager(context) }
    val isBiometricAvailable = remember { biometricManager.isBiometricAvailable() }

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    val maxAttempts = 5

    // Determine if fingerprint is relevant for this lock type
    val isFingerprintMode = settings.lockType == "Fingerprint" || settings.lockType == "PIN + Fingerprint"
    val isFingerprintOnly = settings.lockType == "Fingerprint"
    val showPinInput = !isFingerprintOnly // Show PIN field only when lock type includes PIN

    // Auto-launch fingerprint — keyed on lockType so it fires when settings actually load
    LaunchedEffect(settings.lockType) {
        if (isBiometricAvailable && isFingerprintMode) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                biometricManager.showBiometricPrompt(
                    activity = activity,
                    onSuccess = { onUnlock() },
                    onError = { /* User cancelled or failed, fall back to UI */ }
                )
            }
        }
    }

    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0F1A), Color(0xFF1A0F2E), Color(0xFF000000))
                ) else Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFingerprintOnly) Icons.Rounded.Fingerprint else Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "App Locked",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (isFingerprintOnly) "Use your fingerprint to unlock AI ZAYA"
                    else "Enter your PIN to unlock AI ZAYA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                // -- Fingerprint-only mode: big fingerprint button as primary --
                if (isFingerprintOnly && isBiometricAvailable) {
                    Button(
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null) {
                                biometricManager.showBiometricPrompt(
                                    activity = activity,
                                    onSuccess = { onUnlock() },
                                    onError = { msg -> error = msg }
                                )
                            } else {
                                error = "Biometric authentication unavailable"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Unlock with Fingerprint",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            error,
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // -- PIN input (shown for "PIN" and "PIN + Fingerprint" modes) --
                if (showPinInput) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6) {
                                pin = it
                                error = ""
                            }
                        },
                        placeholder = { Text("Enter PIN", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            errorBorderColor = Color.Red
                        ),
                        isError = error.isNotEmpty()
                    )

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            error,
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (attempts > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Attempts remaining: ${maxAttempts - attempts}",
                            color = if (attempts >= 3) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Unlock Button (PIN)
                    Button(
                        onClick = {
                            when {
                                pin.isEmpty() -> {
                                    error = "Please enter your PIN"
                                }
                                attempts >= maxAttempts -> {
                                    error = "Too many attempts. Please restart the app."
                                }
                                pin == settings.pinCode -> {
                                    onUnlock()
                                }
                                settings.pinCode.isEmpty() -> {
                                    Toast.makeText(context, "No PIN configured. Please set a PIN in Settings.", Toast.LENGTH_LONG).show()
                                    onUnlock()
                                }
                                else -> {
                                    attempts++
                                    error = "Incorrect PIN. Please try again."
                                    pin = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        enabled = attempts < maxAttempts
                    ) {
                        Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Fingerprint as secondary option for "PIN + Fingerprint"
                    if (isFingerprintMode && isBiometricAvailable) {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    biometricManager.showBiometricPrompt(
                                        activity = activity,
                                        onSuccess = { onUnlock() },
                                        onError = { msg -> error = msg }
                                    )
                                } else {
                                    error = "Biometric authentication unavailable"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Fingerprint,
                                contentDescription = "Fingerprint",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Fingerprint", color = NeonCyan, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

