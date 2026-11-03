package com.zayaai.app

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.zayaai.app.ui.theme.LocalIsDarkTheme
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

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

    val isBiometricType = settings.lockType.equals("Biometric", ignoreCase = true) ||
            settings.lockType.equals("Fingerprint", ignoreCase = true) ||
            settings.lockType.contains("Fingerprint", ignoreCase = true)

    // Helper to prompt biometrics
    val triggerBiometricAuth: () -> Unit = {
        val activity = context.findFragmentActivity()
        if (activity != null) {
            error = ""
            biometricManager.showBiometricPrompt(
                activity = activity,
                onSuccess = { onUnlock() },
                onError = { msg ->
                    if (msg != "Cancelled") {
                        error = msg
                    }
                }
            )
        } else {
            error = "Biometric prompt unavailable"
        }
    }

    // Auto-launch fingerprint when screen appears or settings load
    LaunchedEffect(isBiometricAvailable, settings.lockType) {
        if (isBiometricAvailable && (isBiometricType || settings.lockType.isEmpty() || settings.lockType == "PIN")) {
            triggerBiometricAuth()
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
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
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
                        if (isBiometricAvailable) Icons.Rounded.Fingerprint else Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "App Locked",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (isBiometricAvailable) "Touch fingerprint sensor or enter PIN"
                    else "Enter your PIN to unlock AI ZAYA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Fingerprint Unlock Button if available
                if (isBiometricAvailable) {
                    Button(
                        onClick = triggerBiometricAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPurple
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Unlock with Fingerprint",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        )
                        Text(
                            "OR ENTER PIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // PIN input fallback
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

                AnimatedVisibility(visible = error.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            error,
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (attempts > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Attempts remaining: ${maxAttempts - attempts}",
                        color = if (attempts >= 3) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                                Toast.makeText(context, "No PIN configured. Unlocked.", Toast.LENGTH_SHORT).show()
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
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    enabled = attempts < maxAttempts
                ) {
                    Text("Unlock with PIN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
