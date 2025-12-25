package com.natrajtechnology.fitfly.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.natrajtechnology.fitfly.data.model.Exercise
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import com.natrajtechnology.fitfly.ui.viewmodel.ExerciseViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.RoutineViewModel

/**
 * Routine Detail Screen
 * Shows routine details with SMS delegation feature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineViewModel: RoutineViewModel,
    exerciseViewModel: ExerciseViewModel,
    routineId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val routines by routineViewModel.routines.collectAsState()
    val exercises by exerciseViewModel.exercises.collectAsState()
    val routine = routines.find { it.id == routineId }
    val context = LocalContext.current
    
    var showSmsDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var gymNote by remember { mutableStateOf("") }
    
    // SMS Dialog
    if (showSmsDialog && routine != null) {
        AlertDialog(
            onDismissRequest = { showSmsDialog = false },
            icon = { Icon(Icons.Default.Send, contentDescription = null) },
            title = { Text("Send via SMS") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Send this workout checklist to someone")
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                    
                    OutlinedTextField(
                        value = gymNote,
                        onValueChange = { gymNote = it },
                        label = { Text("Optional Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val routineExercises = exercises.filter { it.id in routine.exerciseIds }
                        val message = buildSmsMessage(routine, routineExercises, gymNote)
                        sendSms(context, phoneNumber, message)
                        showSmsDialog = false
                    },
                    enabled = phoneNumber.isNotBlank()
                ) {
                    Text("Send SMS")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (routine == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading routine...")
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routine Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showSmsDialog = true }) {
                        Icon(Icons.Default.Send, contentDescription = "Send via SMS")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Routine Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (routine.description.isNotEmpty()) {
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Completion status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (routine.isCompleted) Icons.Default.CheckCircle 
                                         else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (routine.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (routine.isCompleted) "Completed" else "In Progress",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Equipment Checklist
            if (routine.requiredEquipment.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Equipment Checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Divider()
                        
                        routine.requiredEquipment.forEach { equipment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = equipment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Exercises
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Exercises (${routine.exerciseIds.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Divider()
                    
                    if (routine.exerciseIds.isEmpty()) {
                        Text(
                            text = "No exercises added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        routine.exerciseIds.forEachIndexed { index, exerciseId ->
                            val exercise = exercises.find { it.id == exerciseId }
                            exercise?.let {
                                ExerciseDetailItem(
                                    exercise = it,
                                    index = index + 1
                                )
                                if (index < routine.exerciseIds.size - 1) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showSmsDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send SMS")
                }
                
                Button(
                    onClick = {
                        routineViewModel.toggleRoutineCompletion(routine.id, routine.isCompleted)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (routine.isCompleted) Icons.Default.Refresh else Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (routine.isCompleted) "Mark Incomplete" else "Mark Complete")
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailItem(
    exercise: Exercise,
    index: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$index. ${exercise.name}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "${exercise.sets} sets × ${exercise.reps} reps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (exercise.instructions.isNotEmpty()) {
            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (exercise.requiredEquipment.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = exercise.requiredEquipment.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun buildSmsMessage(
    routine: WorkoutRoutine,
    exercises: List<Exercise>,
    note: String
): String {
    val builder = StringBuilder()
    builder.append("🏋️ FitLife Workout: ${routine.name}\n\n")
    
    if (routine.description.isNotEmpty()) {
        builder.append("${routine.description}\n\n")
    }
    
    builder.append("📋 EXERCISES:\n")
    exercises.forEachIndexed { index, exercise ->
        builder.append("${index + 1}. ${exercise.name}\n")
        builder.append("   ${exercise.sets}×${exercise.reps}\n")
    }
    
    if (routine.requiredEquipment.isNotEmpty()) {
        builder.append("\n🎒 EQUIPMENT:\n")
        routine.requiredEquipment.forEach { equipment ->
            builder.append("• $equipment\n")
        }
    }
    
    if (note.isNotEmpty()) {
        builder.append("\n💡 NOTE:\n$note\n")
    }
    
    builder.append("\n✨ Sent from FitLife App")
    
    return builder.toString()
}

private fun sendSms(context: android.content.Context, phoneNumber: String, message: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
