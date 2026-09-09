package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudyMaterialsScreen(
    adminViewModel: AdminViewModel,
    paddingValues: PaddingValues
) {
    val materials = adminViewModel.allStudyMaterials
    val isLoading = adminViewModel.isLoading
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        adminViewModel.loadAllStudyMaterials()
        adminViewModel.loadAllUserPerformance()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Study Materials Admin",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NeonCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Materials", color = if (selectedTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Activity", color = if (selectedTab == 1) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                } else {
                    when (selectedTab) {
                        0 -> MaterialsTab(materials, adminViewModel)
                        1 -> ActivityTab(adminViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialsTab(
    materials: List<FirestoreStudyMaterial>,
    adminViewModel: AdminViewModel
) {
    if (materials.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Folder, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No study materials uploaded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${materials.size} materials uploaded",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(materials) { material ->
                AdminMaterialCard(material, onDelete = {
                    adminViewModel.deleteStudyMaterial(material.id)
                })
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AdminMaterialCard(
    material: FirestoreStudyMaterial,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (material.fileType) {
                        "pdf" -> Icons.Rounded.PictureAsPdf
                        "image" -> Icons.Rounded.Image
                        "text" -> Icons.Rounded.Description
                        else -> Icons.Rounded.Edit
                    },
                    null,
                    tint = when (material.fileType) {
                        "pdf" -> Color(0xFFFF5252)
                        "image" -> NeonCyan
                        "text" -> Color(0xFF00E676)
                        else -> Color(0xFFFFD600)
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(material.title.ifBlank { "Untitled" }, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    Text(
                        "By: ${material.userEmail} • ${material.fileType.uppercase()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { showDetails = !showDetails }) {
                    Icon(
                        if (showDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF5252))
                }
            }

            if (showDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Extracted Text Preview:", color = NeonCyan, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    material.extractedText.take(400) + if (material.extractedText.length > 400) "..." else "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Material", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("This will permanently delete this material and all generated content.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun ActivityTab(adminViewModel: AdminViewModel) {
    val performances = adminViewModel.allPerformance

    if (performances.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Analytics, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No quiz activity yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "${performances.size} quiz attempts recorded",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(performances) { perf ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val percent = if (perf.totalQuestions > 0) (perf.correctAnswers * 100) / perf.totalQuestions else 0
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    when {
                                        percent >= 80 -> Color(0xFF00E676).copy(alpha = 0.2f)
                                        percent >= 50 -> Color(0xFFFFD600).copy(alpha = 0.2f)
                                        else -> Color(0xFFFF5252).copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$percent%",
                                color = when {
                                    percent >= 80 -> Color(0xFF00E676)
                                    percent >= 50 -> Color(0xFFFFD600)
                                    else -> Color(0xFFFF5252)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "User: ${perf.userId.take(12)}...",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                "${perf.correctAnswers}/${perf.totalQuestions} correct • ${perf.difficulty.replaceFirstChar { it.uppercase() }}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}


