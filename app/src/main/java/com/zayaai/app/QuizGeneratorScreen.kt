package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.NeonCyan
import com.zayaai.app.ui.theme.NeonPurple

@Composable
fun QuizGeneratorScreen(viewModel: QuizGeneratorViewModel, paddingValues: PaddingValues) {
    var topic by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "What topic should the quiz be about?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            placeholder = { Text("Enter a topic (e.g., Photosynthesis, World War II...)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GradientButton(
            text = "Generate Quiz",
            isLoading = viewModel.isLoading,
            onClick = { viewModel.generateQuiz(topic) }
        )
        
        if (viewModel.generatedQuiz.isNotBlank()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Your Quiz",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                MarkdownText(
                    text = viewModel.generatedQuiz,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.clear() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text("Start Over", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

