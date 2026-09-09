package com.zayaai.app

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*
import androidx.compose.material3.MaterialTheme

@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    // Focus requesters for each field
    val nameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (LocalIsDarkTheme.current) Brush.verticalGradient(colors = listOf(Color(0xFF0F0F1A), Color(0xFF1A0F2E), Color(0xFF000000))) else Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
            )
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Enhanced Logo with Glow (matching login screen)
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { -40 }
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonPurple.copy(alpha = 0.4f),
                                        NeonCyan.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { 30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Create Account",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Join the AI ZAYA universe",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                        color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start your AI-powered learning journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (LocalIsDarkTheme.current) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fields with keyboard navigation
            ZayaTextField(
                fullName, { fullName = it }, "Full Name", Icons.Rounded.Person,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                focusRequester = nameFocus
            )
            Spacer(modifier = Modifier.height(14.dp))

            ZayaTextField(
                email, { email = it }, "Email Address", Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                focusRequester = emailFocus
            )
            Spacer(modifier = Modifier.height(14.dp))

            ZayaTextField(
                password, { password = it }, "Password", Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
                focusRequester = passwordFocus
            )
            Spacer(modifier = Modifier.height(14.dp))

            ZayaTextField(
                repeatPassword, { repeatPassword = it }, "Confirm Password", Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = confirmPasswordFocus
            )

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton("Create Account", isLoading = authViewModel.isLoading, onClick = {
                focusManager.clearFocus()
                if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@GradientButton
                }
                if (password != repeatPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@GradientButton
                }
                authViewModel.register(fullName, email, password) { result ->
                    result.onSuccess { onRegisterSuccess() }
                        .onFailure { Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
                }
            })

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account? ",
                    color = if (LocalIsDarkTheme.current) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Login",
                    color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToLogin() },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Focus requesters
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (LocalIsDarkTheme.current) Brush.verticalGradient(colors = listOf(Color(0xFF0F0F1A), Color(0xFF1A0F2E), Color(0xFF000000))) else Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
            )
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Enhanced Logo with Glow
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { -40 }
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonPurple.copy(alpha = 0.5f),
                                        NeonCyan.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(55.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { 30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "AI ZAYA",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        ),
                        color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your Futuristic AI Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                        color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Master your subjects with AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (LocalIsDarkTheme.current) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Email field — Next moves to password
            ZayaTextField(
                email, { email = it }, "Email Address", Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                focusRequester = emailFocus
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Password field — Done dismisses keyboard
            ZayaTextField(
                password, { password = it }, "Password", Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = passwordFocus
            )

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton("Login", isLoading = authViewModel.isLoading, onClick = {
                focusManager.clearFocus()
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    return@GradientButton
                }
                authViewModel.login(email, password) { result ->
                    result.onSuccess { role ->
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(role)
                    }
                    .onFailure { Toast.makeText(context, "Login Failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
                }
            })

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    color = if (LocalIsDarkTheme.current) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Register",
                    color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToRegister() },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Forgot Password?",
                color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToForgotPassword() },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }

    // Focus requester
    val emailFocus = remember { FocusRequester() }

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (LocalIsDarkTheme.current) Brush.verticalGradient(colors = listOf(Color(0xFF0F0F1A), Color(0xFF1A0F2E), Color(0xFF000000))) else Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background))
            )
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Enhanced Logo with Glow
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { -40 }
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonPurple.copy(alpha = 0.5f),
                                        NeonCyan.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.LockReset,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(55.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { 30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Reset Password",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = if (LocalIsDarkTheme.current) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Forgot your password?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                        color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter your email address and we'll send you a link to reset your password",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (LocalIsDarkTheme.current) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Email field
            ZayaTextField(
                email, { email = it }, "Email Address", Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton("Send Reset Link", isLoading = authViewModel.isLoading, onClick = {
                focusManager.clearFocus()
                if (email.isBlank()) {
                    Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
                    return@GradientButton
                }
                authViewModel.resetPassword(email) { result ->
                    result.onSuccess {
                        Toast.makeText(context, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    }
                    .onFailure { Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
                }
            })

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Back to Login",
                color = if (LocalIsDarkTheme.current) NeonCyan else MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateBack() },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
