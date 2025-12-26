package com.natrajtechnology.fitfly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.natrajtechnology.fitfly.data.model.Exercise
import com.natrajtechnology.fitfly.data.model.GeoTag
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import com.natrajtechnology.fitfly.ui.components.LocationPicker
import com.natrajtechnology.fitfly.ui.viewmodel.ExerciseViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.RoutineViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.UiState

/**
 * Add/Edit Routine Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRoutineScreen(
    routineViewModel: RoutineViewModel,
    exerciseViewModel: ExerciseViewModel,
    routineId: String?,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedExercises by remember { mutableStateOf<List<String>>(emptyList()) }
    var equipmentInput by remember { mutableStateOf("") }
    var equipmentList by remember { mutableStateOf<List<String>>(emptyList()) }
    var geoTag by remember { mutableStateOf<GeoTag?>(null) }
    var showExerciseSelector by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var originalRoutine by remember { mutableStateOf<WorkoutRoutine?>(null) }
    
    val uiState by routineViewModel.uiState.collectAsState()
    val routines by routineViewModel.routines.collectAsState()
    val exercises by exerciseViewModel.exercises.collectAsState()
    
    val isEditMode = routineId != null
    
    // Load existing routine if in edit mode
    LaunchedEffect(routineId, routines) {
        if (routineId != null) {
            // Try to find in the loaded list first
            var routine = routines.find { it.id == routineId }
            
            // If not found in list, fetch directly from repository
            if (routine == null) {
                val result = routineViewModel.getRoutineById(routineId)
                result.onSuccess { fetchedRoutine ->
                    routine = fetchedRoutine
                }
            }
            
            routine?.let {
                originalRoutine = it
                name = it.name
                description = it.description
                selectedExercises = it.exerciseIds
                equipmentList = it.requiredEquipment
                geoTag = it.geoTag
            }
        }
    }
    
    // Auto-generate equipment list from selected exercises
    LaunchedEffect(selectedExercises) {
        val autoEquipment = exercises
            .filter { it.id in selectedExercises }
            .flatMap { it.requiredEquipment }
            .distinct()
        equipmentList = (equipmentList + autoEquipment).distinct()
    }
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                routineViewModel.clearUiState()
                onSaveSuccess()
            }
            is UiState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }
    
    // Exercise Selector Dialog
    if (showExerciseSelector) {
        AlertDialog(
            onDismissRequest = { showExerciseSelector = false },
            title = { Text("Select Exercises") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (exercises.isEmpty()) {
                        Text("No exercises available. Create some exercises first!")
                    } else {
                        exercises.forEach { exercise ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exercise.name,
                                    modifier = Modifier.weight(1f)
                                )
                                Checkbox(
                                    checked = exercise.id in selectedExercises,
                                    onCheckedChange = { checked ->
                                        selectedExercises = if (checked) {
                                            selectedExercises + exercise.id
                                        } else {
                                            selectedExercises.filter { it != exercise.id }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExerciseSelector = false }) {
                    Text("Done")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Routine" else "Create Routine") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Routine Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
                        // Location Tag
            Text(
                text = "Location (Optional)",
                style = MaterialTheme.typography.titleSmall
            )
            LocationPicker(
                currentGeoTag = geoTag,
                onGeoTagChange = { geoTag = it },
                modifier = Modifier.fillMaxWidth()
            )
                        // Location Tag
            Text(
                text = "Location (Optional)",
                style = MaterialTheme.typography.titleSmall
            )
            LocationPicker(
                currentGeoTag = geoTag,
                onGeoTagChange = { geoTag = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Exercise Selection
            Text(
                text = "Exercises (${selectedExercises.size} selected)",
                style = MaterialTheme.typography.titleSmall
            )
            
            OutlinedButton(
                onClick = { showExerciseSelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Exercises")
            }
            
            // Selected Exercises List
            if (selectedExercises.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedExercises.forEach { exerciseId ->
                            val exercise = exercises.find { it.id == exerciseId }
                            exercise?.let {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${it.name} (${it.sets}×${it.reps})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedExercises = selectedExercises.filter { id -> id != exerciseId }
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Equipment Section
            Text(
                text = "Required Equipment",
                style = MaterialTheme.typography.titleSmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = equipmentInput,
                    onValueChange = { equipmentInput = it },
                    label = { Text("Add Equipment") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Button(
                    onClick = {
                        if (equipmentInput.isNotBlank()) {
                            equipmentList = (equipmentList + equipmentInput.trim()).distinct()
                            equipmentInput = ""
                        }
                    },
                    enabled = equipmentInput.isNotBlank()
                ) {
                    Text("Add")
                }
            }
            
            // Equipment List
            if (equipmentList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    equipmentList.forEach { equipment ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(equipment) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        equipmentList = equipmentList.filter { it != equipment }
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    val routine = if (isEditMode && originalRoutine != null) {
                        // Preserve original fields when updating
                        originalRoutine!!.copy(
                            name = name,
                            description = description,
                            exerciseIds = selectedExercises,
                            requiredEquipment = equipmentList,
                            geoTag = geoTag
                        )
                    } else {
                        // Create new routine
                        WorkoutRoutine(
                            id = routineId ?: "",
                            name = name,
                            description = description,
                            exerciseIds = selectedExercises,
                            requiredEquipment = equipmentList,
                            geoTag = geoTag
                        )
                    }
                    
                    if (isEditMode) {
                        routineViewModel.updateRoutine(routine)
                    } else {
                        routineViewModel.addRoutine(routine)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Update Routine" else "Create Routine")
                }
            }
            
            // Error Message
            if (uiState is UiState.Error) {
                Text(
                    text = (uiState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
