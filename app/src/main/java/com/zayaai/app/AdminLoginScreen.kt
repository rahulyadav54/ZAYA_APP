package com.zayaai.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Email
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminLoginScreen(
    adminViewModel: AdminViewModel,
    onLoginSuccess: () -> Unit,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Admin Portal",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Authorized Access Only",
                style = MaterialTheme.typography.bodyMedium,
                color = NeonPurple
            )

            Spacer(modifier = Modifier.height(48.dp))

            ZayaTextField(email, { email = it }, "Admin Email", Icons.Rounded.Email, keyboardType = KeyboardType.Email)
            Spacer(modifier = Modifier.height(16.dp))
            ZayaTextField(password, { password = it }, "Password", Icons.Rounded.Lock, isPassword = true)

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton(
                text = "Login as Admin",
                isLoading = isLoading,
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                        return@GradientButton
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            Firebase.auth.signInWithEmailAndPassword(email, password).await()
                            val isAdmin = adminViewModel.checkAdminRole()
                            if (isAdmin) {
                                onLoginSuccess()
                            } else {
                                Firebase.auth.signOut()
                                Toast.makeText(context, "Access Denied: Not an admin", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}

