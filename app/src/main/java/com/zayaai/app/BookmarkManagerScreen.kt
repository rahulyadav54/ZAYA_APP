package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zayaai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkManagerScreen(viewModel: BookmarkViewModel, paddingValues: PaddingValues) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedBookmark by remember { mutableStateOf<BookmarkEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (bookmarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No bookmarks found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookmarks) { bookmark ->
                        BookmarkCard(
                            bookmark = bookmark,
                            onClick = {
                                // Open URL or show details
                            },
                            onDelete = { viewModel.deleteBookmark(bookmark) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                selectedBookmark = null
                showDialog = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = NeonCyan
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Bookmark", tint = Color.Black)
        }

        if (showDialog) {
            BookmarkEditDialog(
                bookmark = selectedBookmark,
                onDismiss = { showDialog = false },
                onSave = { title, url ->
                    if (selectedBookmark == null) {
                        viewModel.addBookmark(title, url)
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun BookmarkCard(bookmark: BookmarkEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = bookmark.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun BookmarkEditDialog(
    bookmark: BookmarkEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(bookmark?.title ?: "") }
    var url by remember { mutableStateOf(bookmark?.url ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bookmark == null) "Add Bookmark" else "Edit Bookmark", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, url) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Save", color = Color.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

