package com.zayaai.app

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.zayaai.app.ui.theme.LocalIsDarkTheme

/**
 * Centered Brand Header: App Logo + AI ZAYA + ALL-IN-ONE AI TOOLS + Powered by ZAYA CODE HUB
 */
@Composable
fun BrandHeader(isDark: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDark) Color(0xFF1E163B) else Color(0xFFF5F3FF))
                .border(
                    1.dp,
                    if (isDark) Color(0xFF3B2F6E) else Color(0xFFDDD6FE),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_zaya_brand_z),
                contentDescription = "AI ZAYA Logo",
                modifier = Modifier.size(54.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "AI ZAYA",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = if (isDark) Color.White else Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "ALL-IN-ONE AI TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Powered by ZAYA CODE HUB",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = if (isDark) Color(0xFFA78BFA).copy(alpha = 0.85f) else Color(0xFF6D28D9)
        )
    }
}

/**
 * Professional User-Facing Dialog displayed when Google Sign-In is unavailable.
 * Completely free of any internal SHA fingerprints, Firebase project IDs, or developer instructions.
 */
@Composable
fun GoogleSignInUnavailableDialog(
    onDismiss: () -> Unit,
    onTryAgain: () -> Unit,
    onUseEmailLogin: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) Color(0xFF141527) else Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Google Sign-In temporarily unavailable",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        text = {
            Text(
                text = "We're experiencing a configuration issue with Google Sign-In. Please try again later or sign in using your email and password.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onTryAgain()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onUseEmailLogin()
                },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Use Email Login",
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * Standard Form Input Field with Rounded Border & Icon
 */
@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null
) {
    val isDark = LocalIsDarkTheme.current
    var passwordVisible by remember { mutableStateOf(false) }

    val fieldModifier = if (focusRequester != null) {
        Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .clip(RoundedCornerShape(12.dp))
    } else {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF94A3B8)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (isPassword) {
                val iconImg = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = iconImg,
                        contentDescription = "Toggle password visibility",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        modifier = fieldModifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
            unfocusedBorderColor = if (isDark) Color(0xFF2E264E) else Color(0xFFE2E8F0),
            focusedContainerColor = if (isDark) Color(0xFF130F24) else Color.White,
            unfocusedContainerColor = if (isDark) Color(0xFF100D1E) else Color(0xFFFBFBFE),
            cursorColor = if (isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
            focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
            unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
        ),
        singleLine = true
    )
}

/**
 * Capsule Purple Action Button (e.g. Sign In →, Create Account →)
 */
@Composable
fun AuthCapsuleButton(
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    ) {
        val bgGradient = if (enabled) {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF6D28D9),
                    Color(0xFF7C3AED),
                    Color(0xFF8B5CF6)
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFCBD5E1),
                    Color(0xFFCBD5E1)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient, shape = RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Continue with Google Button (Official Google 'G' icon + Crisp Surface)
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        onClick = { if (enabled && !isLoading) onClick() },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF1A162B) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0xFF322A4E) else Color(0xFFE2E8F0)
        ),
        shadowElevation = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF7C3AED),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Signing in with Google...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_g),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            }
        }
    }
}

// =========================================================================
// 1. LOGIN SCREEN (Clean, Modern & Light)
// =========================================================================
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = LocalIsDarkTheme.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showUnavailableDialog by remember { mutableStateOf(false) }

    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    // Official Google Sign-In Activity Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    authViewModel.signInWithGoogle(account) { loginResult ->
                        loginResult.onSuccess { role ->
                            Toast.makeText(context, "Welcome back, ${account.displayName ?: "User"}!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(role)
                        }.onFailure {
                            Toast.makeText(context, "Google Sign-In failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "No Google account selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    10, 12500 -> { // DEVELOPER_ERROR / CONFIG_ERROR
                        android.util.Log.e("AuthScreens", "Google Sign-In configuration error (${e.statusCode}): ${e.message}")
                        showUnavailableDialog = true
                    }
                    12501 -> { // SIGN_IN_CANCELLED by user
                        Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
                    }
                    7 -> { // NETWORK_ERROR
                        Toast.makeText(context, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        android.util.Log.e("AuthScreens", "Google Sign-In error (${e.statusCode}): ${e.message}")
                        Toast.makeText(context, "Google Sign-In (${e.statusCode}): ${e.localizedMessage ?: "Please try again"}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthScreens", "Google Sign-In exception: ${e.message}")
                Toast.makeText(context, "Google Sign-In error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    if (showUnavailableDialog) {
        GoogleSignInUnavailableDialog(
            onDismiss = { showUnavailableDialog = false },
            onTryAgain = {
                if (!authViewModel.isLoading) {
                    try {
                        val intent = authViewModel.getGoogleSignInIntent()
                        googleSignInLauncher.launch(intent)
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Google Sign-In: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onUseEmailLogin = {
                emailFocus.requestFocus()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF090614) else Color(0xFFFFFFFF))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Centered Brand Header
            BrandHeader(isDark = isDark)

            Spacer(modifier = Modifier.height(26.dp))

            // Welcome Header
            Text(
                text = "Welcome to AI ZAYA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to continue your AI learning journey.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Email Field
            AuthInputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            AuthInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = passwordFocus
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remember Me (Left) & Forgot Password (Right) Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        rememberMe = !rememberMe
                    }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF7C3AED),
                            uncheckedColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                    )
                }

                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier
                        .clickable { onNavigateToForgotPassword() }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Button: "Sign In →"
            AuthCapsuleButton(
                text = "Sign In →",
                isLoading = authViewModel.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                        return@AuthCapsuleButton
                    }
                    authViewModel.login(email.trim(), password) { result ->
                        result.onSuccess { role ->
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(role)
                        }.onFailure {
                            Toast.makeText(context, "Login Failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // "OR" Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (isDark) Color(0xFF2E264E) else Color(0xFFE2E8F0)
                )
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (isDark) Color(0xFF2E264E) else Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Google Sign-In Button
            GoogleSignInButton(
                onClick = {
                    if (authViewModel.isLoading) return@GoogleSignInButton
                    try {
                        val intent = authViewModel.getGoogleSignInIntent()
                        googleSignInLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !authViewModel.isLoading,
                isLoading = authViewModel.isLoading
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Footer Switch to Sign Up
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Powered by ZAYA CODE HUB",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =========================================================================
// 2. CREATE ACCOUNT SCREEN (Clean, Modern & Light)
// =========================================================================
@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = LocalIsDarkTheme.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showUnavailableDialog by remember { mutableStateOf(false) }

    val nameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }

    // Google Sign-In Launcher for Registration as well
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    authViewModel.signInWithGoogle(account) { loginResult ->
                        loginResult.onSuccess {
                            Toast.makeText(context, "Welcome, ${account.displayName ?: "User"}!", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        }.onFailure {
                            Toast.makeText(context, "Google Sign-In failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "No Google account selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    10, 12500 -> { // DEVELOPER_ERROR / CONFIG_ERROR
                        android.util.Log.e("AuthScreens", "Google Sign-In configuration error (${e.statusCode}): ${e.message}")
                        showUnavailableDialog = true
                    }
                    12501 -> { // SIGN_IN_CANCELLED by user
                        Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
                    }
                    7 -> { // NETWORK_ERROR
                        Toast.makeText(context, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        android.util.Log.e("AuthScreens", "Google Sign-In error (${e.statusCode}): ${e.message}")
                        Toast.makeText(context, "Google Sign-In (${e.statusCode}): ${e.localizedMessage ?: "Please try again"}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthScreens", "Google Sign-In exception: ${e.message}")
                Toast.makeText(context, "Google Sign-In error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    if (showUnavailableDialog) {
        GoogleSignInUnavailableDialog(
            onDismiss = { showUnavailableDialog = false },
            onTryAgain = {
                if (!authViewModel.isLoading) {
                    try {
                        val intent = authViewModel.getGoogleSignInIntent()
                        googleSignInLauncher.launch(intent)
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Google Sign-In: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onUseEmailLogin = {
                nameFocus.requestFocus()
            }
        )
    }

    // Password criteria evaluation
    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    val metCount = listOf(hasMinLength, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    val allRequirementsMet = metCount == 5
    val passwordsMatch = confirmPassword.isNotEmpty() && password == confirmPassword

    val strengthLabel = when {
        password.isEmpty() -> "Empty"
        metCount <= 2 -> "Weak"
        metCount in 3..4 -> "Fair"
        else -> "Strong"
    }
    val strengthColor = when {
        password.isEmpty() -> Color.Gray
        metCount <= 2 -> Color(0xFFEF4444)
        metCount in 3..4 -> Color(0xFFF59E0B)
        else -> Color(0xFF16A34A)
    }

    val isFormValid = fullName.isNotBlank() && email.isNotBlank() && allRequirementsMet && passwordsMatch

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF090614) else Color(0xFFFFFFFF))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 16.dp)
        ) {
            // Top Back Arrow Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            BrandHeader(isDark = isDark)

            Spacer(modifier = Modifier.height(20.dp))

            // Heading: "Create your account"
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Get started with AI ZAYA for smart learning and tools.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Full Name
            AuthInputField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Full Name",
                icon = Icons.Rounded.Person,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                focusRequester = nameFocus
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Email Address
            AuthInputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Password
            AuthInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
                focusRequester = passwordFocus
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Strength Indicator: Weak / Fair / Strong
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Password Strength: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = strengthLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = strengthColor
                    )
                }

                // 3 Segmented Strength Bars
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val activeSegments = when {
                        metCount <= 2 -> 1
                        metCount in 3..4 -> 2
                        else -> 3
                    }
                    repeat(3) { idx ->
                        val isActive = password.isNotEmpty() && idx < activeSegments
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isActive) strengthColor
                                    else if (isDark) Color(0xFF2E264E)
                                    else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Checklist (Neutral indicators for incomplete, green check for satisfied)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF130E26) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF2B214E) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    RequirementRow(text = "At least 8 characters", isMet = hasMinLength, isDark = isDark)
                    RequirementRow(text = "One uppercase letter (A - Z)", isMet = hasUpper, isDark = isDark)
                    RequirementRow(text = "One lowercase letter (a - z)", isMet = hasLower, isDark = isDark)
                    RequirementRow(text = "One number (0 - 9)", isMet = hasDigit, isDark = isDark)
                    RequirementRow(text = "One special symbol (!@#$%...)", isMet = hasSpecial, isDark = isDark)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Confirm Password
            AuthInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = confirmPasswordFocus
            )

            // Confirm Password matching indicator
            if (confirmPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (passwordsMatch) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = if (passwordsMatch) Color(0xFF16A34A) else Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (passwordsMatch) "Passwords match" else "Passwords do not match",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (passwordsMatch) Color(0xFF16A34A) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Button: "Create Account →"
            AuthCapsuleButton(
                text = "Create Account →",
                isLoading = authViewModel.isLoading,
                enabled = isFormValid,
                onClick = {
                    focusManager.clearFocus()
                    if (!isFormValid) {
                        Toast.makeText(context, "Please fulfill all required fields and password criteria", Toast.LENGTH_SHORT).show()
                        return@AuthCapsuleButton
                    }

                    authViewModel.register(fullName.trim(), email.trim(), password) { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        }.onFailure {
                            Toast.makeText(context, "Registration failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "OR" Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (isDark) Color(0xFF2E264E) else Color(0xFFE2E8F0)
                )
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (isDark) Color(0xFF2E264E) else Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign-In Button on Registration
            GoogleSignInButton(
                onClick = {
                    if (authViewModel.isLoading) return@GoogleSignInButton
                    try {
                        val intent = authViewModel.getGoogleSignInIntent()
                        googleSignInLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !authViewModel.isLoading,
                isLoading = authViewModel.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Switch to Sign In
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Powered by ZAYA CODE HUB",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Single Requirement Item with Neutral Circle / Green Check
 */
@Composable
private fun RequirementRow(
    text: String,
    isMet: Boolean,
    isDark: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isMet) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF16A34A) else if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                fontWeight = if (isMet) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isMet) {
                if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
            } else {
                if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            }
        )
    }
}

// =========================================================================
// 3. FORGOT PASSWORD SCREEN (Clean, Modern & Light)
// =========================================================================
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = LocalIsDarkTheme.current
    var email by remember { mutableStateOf("") }
    val emailFocus = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF090614) else Color(0xFFFFFFFF))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 20.dp)
        ) {
            // Top Back Arrow Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            BrandHeader(isDark = isDark)

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your registered email address to receive password reset instructions.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                ),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            AuthInputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthCapsuleButton(
                text = "Send Reset Link →",
                isLoading = authViewModel.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank()) {
                        Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
                        return@AuthCapsuleButton
                    }
                    authViewModel.resetPassword(email.trim()) { result ->
                        result.onSuccess {
                            Toast.makeText(
                                context,
                                "Password reset email sent! Check your inbox.",
                                Toast.LENGTH_LONG
                            ).show()
                            onNavigateBack()
                        }.onFailure {
                            Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Return to Sign In",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color(0xFF7C3AED),
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(8.dp)
            )
        }
    }
}
