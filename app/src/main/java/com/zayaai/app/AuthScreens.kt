package com.zayaai.app

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import com.zayaai.app.ui.theme.LocalIsDarkTheme

/**
 * Professional Input Field with Label Above
 */
@Composable
private fun ProfessionalInputField(
    label: String,
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
            modifier = Modifier.padding(bottom = 6.dp)
        )

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
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                    modifier = Modifier.size(19.dp)
                )
            },
            trailingIcon = {
                if (isPassword) {
                    val iconImg = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = iconImg,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
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
                focusedBorderColor = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                unfocusedBorderColor = if (isDark) Color(0xFF2A2648) else Color(0xFFCBD5E1),
                focusedContainerColor = if (isDark) Color(0xFF16142A) else Color(0xFFFFFFFF),
                unfocusedContainerColor = if (isDark) Color(0xFF131126) else Color(0xFFF8FAFC),
                cursorColor = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
            ),
            singleLine = true
        )
    }
}

/**
 * Clean & Professional Primary Button (No childish rockets, pure elegance)
 */
@Composable
private fun ProfessionalButton(
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF6366F1)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 1. LOGIN SCREEN (Clean, Modern & Professional)
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

    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF090A14),
                Color(0xFF120E28),
                Color(0xFF090A14)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF9F9FE),
                Color(0xFFF3F1FD),
                Color(0xFFFFFFFF)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Official Brand Logo (Exact squircle logo uploaded by user)
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = "AI ZAYA Logo",
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Typography Header
            Text(
                text = "Welcome to AI ZAYA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to continue your AI learning journey",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Form Fields
            ProfessionalInputField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "name@example.com",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(18.dp))

            ProfessionalInputField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = passwordFocus
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Forgot Password (right aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                    modifier = Modifier
                        .clickable { onNavigateToForgotPassword() }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Sign In Button
            ProfessionalButton(
                text = "Sign In",
                isLoading = authViewModel.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                        return@ProfessionalButton
                    }
                    authViewModel.login(email.trim(), password) { result ->
                        result.onSuccess { role ->
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(role)
                        }
                        .onFailure {
                            Toast.makeText(context, "Login Failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Clean Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (isDark) Color(0xFF1E1C33) else Color(0xFFE2E8F0)
                )
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (isDark) Color(0xFF1E1C33) else Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer Switch to Sign Up
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .padding(4.dp)
                )
            }
        }
    }
}

// =========================================================================
// 2. REGISTRATION SCREEN (Clean, Modern & Professional)
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
    var repeatPassword by remember { mutableStateOf("") }

    val nameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF090A14),
                Color(0xFF120E28),
                Color(0xFF090A14)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF9F9FE),
                Color(0xFFF3F1FD),
                Color(0xFFFFFFFF)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Official Brand Logo
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = "AI ZAYA Logo",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Header Typography
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Get started with AI ZAYA for smart learning & tools",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Fields
            ProfessionalInputField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "John Doe",
                icon = Icons.Rounded.Person,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                focusRequester = nameFocus
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfessionalInputField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "name@example.com",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfessionalInputField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Create a strong password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
                focusRequester = passwordFocus
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Password Requirements Feedback
            PasswordRequirementsList(password = password)

            Spacer(modifier = Modifier.height(14.dp))

            ProfessionalInputField(
                label = "Confirm Password",
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                placeholder = "Re-enter your password",
                icon = Icons.Rounded.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = confirmPasswordFocus
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Create Account Button (NO rocket, pure professional)
            ProfessionalButton(
                text = "Create Account",
                isLoading = authViewModel.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@ProfessionalButton
                    }

                    val hasMinLength = password.length >= 8
                    val hasUpper = password.any { it.isUpperCase() }
                    val hasLower = password.any { it.isLowerCase() }
                    val hasDigit = password.any { it.isDigit() }
                    val hasSpecial = password.any { !it.isLetterOrDigit() }
                    val isPasswordValid = hasMinLength && hasUpper && hasLower && hasDigit && hasSpecial

                    if (!isPasswordValid) {
                        Toast.makeText(
                            context,
                            "Password must meet all 5 requirements below",
                            Toast.LENGTH_LONG
                        ).show()
                        return@ProfessionalButton
                    }

                    if (password != repeatPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@ProfessionalButton
                    }

                    authViewModel.register(fullName.trim(), email.trim(), password) { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        }
                        .onFailure {
                            Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Switch to Sign In
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(
                    text = "Sign in",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// =========================================================================
// 3. FORGOT PASSWORD SCREEN (Clean, Modern & Professional)
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

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF090A14),
                Color(0xFF120E28),
                Color(0xFF090A14)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF9F9FE),
                Color(0xFFF3F1FD),
                Color(0xFFFFFFFF)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Top Back Arrow Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF1B1832) else Color(0xFFEDE9FE),
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else Color(0xFF4338CA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Official Brand Logo
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = "AI ZAYA Logo",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Reset your password",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your registered email address and we will send you a reset link.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfessionalInputField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "name@example.com",
                icon = Icons.Rounded.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                focusRequester = emailFocus
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfessionalButton(
                text = "Send Reset Link",
                isLoading = authViewModel.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank()) {
                        Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
                        return@ProfessionalButton
                    }
                    authViewModel.resetPassword(email.trim()) { result ->
                        result.onSuccess {
                            Toast.makeText(
                                context,
                                "Password reset email sent! Check your inbox.",
                                Toast.LENGTH_LONG
                            ).show()
                            onNavigateBack()
                        }
                        .onFailure {
                            Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Return to sign in",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color(0xFF8B5CF6) else Color(0xFF6366F1),
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(8.dp)
            )
        }
    }
}

// =========================================================================
// 4. LIVE PASSWORD REQUIREMENTS COMPOSABLES
// =========================================================================
@Composable
fun PasswordRequirementsList(password: String) {
    val isDark = LocalIsDarkTheme.current
    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PasswordCriteriaItem("8+ chars", hasMinLength, modifier = Modifier.weight(1f))
            PasswordCriteriaItem("Uppercase", hasUpper, modifier = Modifier.weight(1f))
            PasswordCriteriaItem("Lowercase", hasLower, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PasswordCriteriaItem("Number (0-9)", hasDigit, modifier = Modifier.weight(1f))
            PasswordCriteriaItem("Symbol (!@#...)", hasSpecial, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PasswordCriteriaItem(
    label: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val activeColor = Color(0xFF10B981)
    val inactiveColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val bgColor = if (isMet) {
        activeColor.copy(alpha = if (isDark) 0.18f else 0.12f)
    } else {
        if (isDark) Color(0xFF18152B) else Color(0xFFF1F5F9)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isMet) Icons.Rounded.CheckCircle else Icons.Rounded.Circle,
                contentDescription = null,
                tint = if (isMet) activeColor else inactiveColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isMet) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isMet) activeColor else inactiveColor,
                maxLines = 1
            )
        }
    }
}
