package com.zayaai.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.zayaai.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Helper to downscale and compress avatar to JPEG
suspend fun compressAvatarImage(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return@withContext null

        val maxDim = 512
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = if (width > maxDim || height > maxDim) {
            maxDim.toFloat() / maxOf(width, height)
        } else 1f

        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
        } else originalBitmap

        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
        output.toByteArray()
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onProfileUpdate: () -> Unit,
    paddingValues: PaddingValues
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userRepo = remember { UserRepository() }

    var userProfile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load full profile details from Firestore
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            isLoading = true
            val profile = withContext(Dispatchers.IO) { userRepo.getUser(uid) }
            userProfile = profile
            isLoading = false
        } else {
            isLoading = false
        }
    }

    val displayName = userProfile?.name?.ifBlank { null }
        ?: currentUser?.displayName?.ifBlank { null }
        ?: authViewModel.displayName
    val email = currentUser?.email ?: userProfile?.email ?: authViewModel.firestoreUserEmail ?: "No email"
    val photoUrl = userProfile?.photoUrl ?: currentUser?.photoUrl?.toString()
    val role = userProfile?.role ?: authViewModel.userRole ?: "Student"
    
    // Creation date
    val creationDateFormatted = remember(currentUser?.metadata?.creationTimestamp) {
        val time = currentUser?.metadata?.creationTimestamp ?: userProfile?.createdAt ?: 0L
        if (time > 0L) {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            "Member since " + sdf.format(Date(time))
        } else {
            "Active Member"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryPurple
                        )
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Profile Photo & Identity Card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF16172A) else Color(0xFFFFFFFF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF242646) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo with gradient ring
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))
                                )
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF101124) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val initials = displayName.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .joinToString("")
                                    .ifBlank { "Z" }

                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp
                                    ),
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Name
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email with Verified lock icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = "Verified",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role & Membership Pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = PrimaryPurple.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = role.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isDark) Color(0xFF20233B) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = creationDateFormatted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button: Edit Profile
                    Button(
                        onClick = { navController.navigate(Screen.EditProfile.route) },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }

            // ── Bio Section (if available) ──
            if (!userProfile?.bio.isNullOrBlank()) {
                ProfileInfoCard(
                    title = "About / Bio",
                    icon = Icons.Rounded.FormatQuote,
                    isDark = isDark
                ) {
                    Text(
                        text = userProfile?.bio ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ── Academic & Organization Information ──
            ProfileInfoCard(
                title = "Academic & Organization",
                icon = Icons.Rounded.School,
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileFieldRow(
                        label = "College / University",
                        value = userProfile?.college?.ifBlank { null } ?: "Not specified",
                        icon = Icons.Rounded.AccountBalance
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                    ProfileFieldRow(
                        label = "Department",
                        value = userProfile?.department?.ifBlank { null } ?: "Not specified",
                        icon = Icons.Rounded.Category
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                    ProfileFieldRow(
                        label = "Year of Study",
                        value = userProfile?.yearOfStudy?.ifBlank { null } ?: "Not specified",
                        icon = Icons.Rounded.CalendarToday
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Contact Information ──
            ProfileInfoCard(
                title = "Contact Information",
                icon = Icons.Rounded.ContactPhone,
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileFieldRow(
                        label = "Email Address",
                        value = email,
                        icon = Icons.Rounded.Email,
                        trailingIcon = {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = "Protected",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    HorizontalDivider(color = if (isDark) Color(0xFF242646) else Color(0xFFF1F5F9))
                    ProfileFieldRow(
                        label = "Phone Number",
                        value = userProfile?.phone?.ifBlank { null } ?: "Not added yet",
                        icon = Icons.Rounded.Phone
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Account Security Quick Link ──
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.Settings.route) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isDark) Color(0xFF16172A) else Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF242646) else Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryPurple.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Security,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Settings & Security",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Password, notifications, theme & permissions",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ── Edit Profile Screen ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onProfileUpdate: () -> Unit,
    paddingValues: PaddingValues
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userRepo = remember { UserRepository() }

    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var collegeInput by remember { mutableStateOf("") }
    var departmentInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var bioInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isPhotoRemoved by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var isLoadingInitial by remember { mutableStateOf(true) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isPhotoRemoved = false
        }
    }

    // Load initial user data
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            val profile = withContext(Dispatchers.IO) { userRepo.getUser(uid) }
            if (profile != null) {
                nameInput = profile.name.ifBlank { currentUser?.displayName ?: "" }
                phoneInput = profile.phone
                collegeInput = profile.college
                departmentInput = profile.department
                yearInput = profile.yearOfStudy
                bioInput = profile.bio
                existingPhotoUrl = profile.photoUrl ?: currentUser?.photoUrl?.toString()
            } else {
                nameInput = currentUser?.displayName ?: ""
                existingPhotoUrl = currentUser?.photoUrl?.toString()
            }
        }
        isLoadingInitial = false
    }

    val email = currentUser?.email ?: "No email"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (nameInput.trim().length < 2) {
                                Toast.makeText(context, "Please enter a valid full name (min 2 characters)", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val uid = currentUser?.uid ?: return@TextButton
                            isSaving = true

                            scope.launch {
                                try {
                                    var finalPhotoUrl: String? = if (isPhotoRemoved) null else existingPhotoUrl

                                    // Upload new image if chosen
                                    if (selectedImageUri != null) {
                                        val compressedBytes = compressAvatarImage(context, selectedImageUri!!)
                                        if (compressedBytes != null) {
                                            try {
                                                val storageRef = FirebaseStorage.getInstance()
                                                    .reference
                                                    .child("avatars")
                                                    .child("${uid}.jpg")
                                                storageRef.putBytes(compressedBytes).await()
                                                finalPhotoUrl = storageRef.downloadUrl.await().toString()
                                            } catch (storageError: Exception) {
                                                // Graceful fallback to Base64 data URI if storage bucket rules prevent upload
                                                val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                                                finalPhotoUrl = "data:image/jpeg;base64,$base64Str"
                                            }
                                        }
                                    }

                                    // 1. Update Firebase Auth Profile
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = nameInput.trim()
                                        if (finalPhotoUrl != null && !finalPhotoUrl.startsWith("data:")) {
                                            photoUri = Uri.parse(finalPhotoUrl)
                                        }
                                    }
                                    currentUser?.updateProfile(profileUpdates)?.await()

                                    // 2. Update Firestore User Document
                                    val result = userRepo.updateUserProfile(
                                        uid = uid,
                                        name = nameInput.trim(),
                                        photoUrl = finalPhotoUrl,
                                        phone = phoneInput.trim(),
                                        college = collegeInput.trim(),
                                        department = departmentInput.trim(),
                                        yearOfStudy = yearInput.trim(),
                                        bio = bioInput.trim()
                                    )

                                    if (result.isSuccess) {
                                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                        onProfileUpdate()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Failed to save profile: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error saving: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryPurple
                            )
                        } else {
                            Text(
                                "Save",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryPurple
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Photo Picker / Avatar Preview ──
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))
                        )
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF101124) else Color(0xFFF1F5F9))
                        .clickable(enabled = !isSaving) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(selectedImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "New Selected Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        !isPhotoRemoved && !existingPhotoUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(existingPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Current Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Rounded.AddAPhoto,
                                contentDescription = "Add photo",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo action buttons (Change / Remove)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !isSaving
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Photo", fontSize = 13.sp, color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                }

                if (selectedImageUri != null || (!isPhotoRemoved && !existingPhotoUrl.isNullOrBlank())) {
                    TextButton(
                        onClick = {
                            selectedImageUri = null
                            isPhotoRemoved = true
                        },
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", fontSize = 13.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Form Fields ──
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Full Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving,
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = PrimaryPurple) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email Field (Protected & Read-Only)
            OutlinedTextField(
                value = email,
                onValueChange = { },
                label = { Text("Email Address") },
                singleLine = true,
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Read only",
                        tint = Color(0xFF94A3B8)
                    )
                },
                supportingText = {
                    Text(
                        "Email is tied to your account and cannot be modified here for security.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving,
                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = PrimaryPurple) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = collegeInput,
                onValueChange = { collegeInput = it },
                label = { Text("College / University / Organization") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving,
                leadingIcon = { Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = PrimaryPurple) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = departmentInput,
                    onValueChange = { departmentInput = it },
                    label = { Text("Department") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSaving,
                    leadingIcon = { Icon(Icons.Rounded.Category, contentDescription = null, tint = PrimaryPurple) }
                )

                OutlinedTextField(
                    value = yearInput,
                    onValueChange = { yearInput = it },
                    label = { Text("Year") },
                    singleLine = true,
                    placeholder = { Text("e.g. 3rd") },
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSaving
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bioInput,
                onValueChange = { bioInput = it },
                label = { Text("Bio / Status") },
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("Tell us a little about your interests or goals...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving,
                leadingIcon = { Icon(Icons.Rounded.Description, contentDescription = null, tint = PrimaryPurple) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons (Save & Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSaving
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (nameInput.trim().length < 2) {
                            Toast.makeText(context, "Please enter a valid full name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val uid = currentUser?.uid ?: return@Button
                        isSaving = true
                        scope.launch {
                            try {
                                var finalPhotoUrl: String? = if (isPhotoRemoved) null else existingPhotoUrl
                                if (selectedImageUri != null) {
                                    val compressedBytes = compressAvatarImage(context, selectedImageUri!!)
                                    if (compressedBytes != null) {
                                        try {
                                            val storageRef = FirebaseStorage.getInstance()
                                                .reference
                                                .child("avatars")
                                                .child("${uid}.jpg")
                                            storageRef.putBytes(compressedBytes).await()
                                            finalPhotoUrl = storageRef.downloadUrl.await().toString()
                                        } catch (storageError: Exception) {
                                            val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                                            finalPhotoUrl = "data:image/jpeg;base64,$base64Str"
                                        }
                                    }
                                }

                                val profileUpdates = userProfileChangeRequest {
                                    displayName = nameInput.trim()
                                    if (finalPhotoUrl != null && !finalPhotoUrl.startsWith("data:")) {
                                        photoUri = Uri.parse(finalPhotoUrl)
                                    }
                                }
                                currentUser?.updateProfile(profileUpdates)?.await()

                                val result = userRepo.updateUserProfile(
                                    uid = uid,
                                    name = nameInput.trim(),
                                    photoUrl = finalPhotoUrl,
                                    phone = phoneInput.trim(),
                                    college = collegeInput.trim(),
                                    department = departmentInput.trim(),
                                    yearOfStudy = yearInput.trim(),
                                    bio = bioInput.trim()
                                )

                                if (result.isSuccess) {
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                    onProfileUpdate()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "Failed to save profile: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error saving: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Reusable Profile Card Components ──

@Composable
private fun ProfileInfoCard(
    title: String,
    icon: ImageVector,
    isDark: Boolean,
    content: @Composable () -> Unit
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
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            content()
        }
    }
}

@Composable
private fun ProfileFieldRow(
    label: String,
    value: String,
    icon: ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

