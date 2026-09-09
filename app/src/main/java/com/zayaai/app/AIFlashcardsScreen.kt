package com.zayaai.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
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
fun AIFlashcardsScreen(
    viewModel: StudyMaterialViewModel,
    paddingValues: PaddingValues
) {
    val cards = viewModel.flashcards
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Style, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No flashcards generated yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return
        }

        // Counter
        Text(
            "Card ${currentIndex + 1} of ${cards.size}",
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / cards.size },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = NeonCyan,
            trackColor = MaterialTheme.colorScheme.surface,
            drawStopIndicator = {}
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Flashcard with flip animation
        val rotation by animateFloatAsState(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = tween(400),
            label = "flip"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { isFlipped = !isFlipped },
            colors = CardDefaults.cardColors(
                containerColor = if (rotation <= 90f) MaterialTheme.colorScheme.surface else NeonPurple.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (rotation <= 90f) NeonCyan.copy(alpha = 0.3f) else NeonPurple.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Flip text so it's not mirrored
                        if (rotation > 90f) rotationY = 180f
                    }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Rounded.HelpOutline,
                            null,
                            tint = NeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("QUESTION", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            cards[currentIndex].frontText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Tap to reveal answer", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    // Back
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Lightbulb,
                            null,
                            tint = NeonPurple,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("ANSWER", color = NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            cards[currentIndex].backText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Tap to see question", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isFlipped = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = if (currentIndex > 0) NeonCyan else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Previous", color = if (currentIndex > 0) MaterialTheme.colorScheme.onBackground else Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    if (currentIndex < cards.size - 1) {
                        currentIndex++
                        isFlipped = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(14.dp),
                enabled = currentIndex < cards.size - 1,
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Text("Next", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}





