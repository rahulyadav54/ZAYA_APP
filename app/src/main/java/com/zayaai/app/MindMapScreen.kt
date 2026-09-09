package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayaai.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun MindMapScreen(viewModel: MindMapViewModel, paddingValues: PaddingValues) {
    val nodes by viewModel.nodes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Canvas for Nodes
        nodes.forEach { node ->
            MindMapNodeItem(
                node = node,
                onPositionChange = { x, y -> viewModel.updateNodePosition(node, x, y) },
                onDelete = { viewModel.deleteNode(node) }
            )
        }

        if (nodes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tap + to create your first idea node", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = NeonCyan
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Node", tint = Color.Black)
        }

        if (showDialog) {
            AddNodeDialog(
                onDismiss = { showDialog = false },
                onSave = { text ->
                    viewModel.addNode(text, 200f, 200f)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun MindMapNodeItem(
    node: MindMapNodeEntity,
    onPositionChange: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(node.positionX) }
    var offsetY by remember { mutableFloatStateOf(node.positionY) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(node.id) {
                detectDragGestures(
                    onDragEnd = { onPositionChange(offsetX, offsetY) }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(node.text, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AddNodeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Idea", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Node Text") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
            )
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSave(text) }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                Text("Add", color = Color.Black)
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

