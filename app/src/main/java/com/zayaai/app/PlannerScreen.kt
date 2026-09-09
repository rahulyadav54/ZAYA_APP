package com.zayaai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zayaai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlannerScreen(viewModel: StudyPlannerViewModel, paddingValues: PaddingValues) {
    val plans by viewModel.studyPlans.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (plans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your study schedule is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plans) { plan ->
                    PlanCard(plan = plan, onDelete = { viewModel.deletePlan(plan) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = NeonCyan
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Plan", tint = Color.Black)
        }

        if (showDialog) {
            AddPlanDialog(
                onDismiss = { showDialog = false },
                onSave = { subject, date, start, end ->
                    viewModel.addPlan(subject, date, start, end)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun PlanCard(plan: StudyPlanEntity, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = NeonCyan)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.subject, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Text(sdf.format(Date(plan.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${plan.startTime} - ${plan.endTime}", style = MaterialTheme.typography.labelSmall, color = NeonPurple)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AddPlanDialog(onDismiss: () -> Unit, onSave: (String, Long, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("10:00 AM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Session", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (subject.isNotBlank()) onSave(subject, System.currentTimeMillis(), startTime, endTime)
            }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                Text("Add", color = Color.Black)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

