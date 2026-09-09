package com.zayaai.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*

@Composable
fun FlashcardsScreen(viewModel: FlashcardsViewModel, paddingValues: PaddingValues) {
    val flashcards by viewModel.flashcards.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (flashcards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No flashcards found. Create one!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Card ${currentIndex + 1} of ${flashcards.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                val currentCard = flashcards[currentIndex]
                FlashcardItem(card = currentCard)

                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text("Previous")
                    }

                    IconButton(onClick = { viewModel.deleteFlashcard(currentCard) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = { if (currentIndex < flashcards.size - 1) currentIndex++ },
                        enabled = currentIndex < flashcards.size - 1,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Next", color = Color.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                IconButton(onClick = { currentIndex = (0 until flashcards.size).random() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Shuffle", tint = NeonPurple)
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = NeonPurple
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Flashcard", tint = Color.White)
        }

        if (showDialog) {
            AddFlashcardDialog(
                onDismiss = { showDialog = false },
                onSave = { front, back ->
                    viewModel.addFlashcard(front, back)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun FlashcardItem(card: FlashcardEntity) {
    var rotated by remember(card.id) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { rotated = !rotated },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonCyan,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

@Composable
fun AddFlashcardDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Flashcard", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Question (Front)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Answer (Back)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (front.isNotBlank() && back.isNotBlank()) onSave(front, back) }, colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

