package com.natrajtechnology.fitfly.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.natrajtechnology.fitfly.data.model.Exercise
import com.natrajtechnology.fitfly.data.model.GeoTag
import com.natrajtechnology.fitfly.ui.components.LocationPicker
import com.natrajtechnology.fitfly.ui.viewmodel.ExerciseViewModel
import com.natrajtechnology.fitfly.ui.viewmodel.UiState
import kotlinx.coroutines.flow.filter

/**
 * Add/Edit Exercise Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExerciseScreen(
    exerciseViewModel: ExerciseViewModel,
    exerciseId: String?,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var equipmentInput by remember { mutableStateOf("") }
    var equipmentList by remember { mutableStateOf<List<String>>(emptyList()) }
    var geoTag by remember { mutableStateOf<GeoTag?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var originalExercise by remember { mutableStateOf<Exercise?>(null) }
    
    val uiState by exerciseViewModel.uiState.collectAsState()
    val exercises by exerciseViewModel.exercises.collectAsState()
    
    val isEditMode = exerciseId != null
    
    // Load existing exercise if in edit mode
    LaunchedEffect(exerciseId, exercises) {
        if (exerciseId != null) {
            // Try to find in the loaded list first
            var exercise = exercises.find { it.id == exerciseId }
            
            // If not found in list, fetch directly from repository
            if (exercise == null) {
                val result = exerciseViewModel.getExerciseById(exerciseId)
                result.onSuccess { fetchedExercise ->
                    exercise = fetchedExercise
                }
            }
            
            exercise?.let {
                originalExercise = it
                name = it.name
                sets = it.sets.toString()
                reps = it.reps.toString()
                instructions = it.instructions
                equipmentList = it.requiredEquipment
                geoTag = it.geoTag
            }
        }
    }
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                exerciseViewModel.clearUiState()
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Exercise" else "Add Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                label = { Text("Exercise Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Sets Field
            OutlinedTextField(
                value = sets,
                onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) sets = it },
                label = { Text("Sets") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // Reps Field
            OutlinedTextField(
                value = reps,
                onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) reps = it },
                label = { Text("Reps") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // Instructions Field
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
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
                            equipmentList = equipmentList + equipmentInput.trim()
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
                    val exercise = if (isEditMode && originalExercise != null) {
                        // Preserve original fields when updating
                        originalExercise!!.copy(
                            name = name,
                            sets = sets.toIntOrNull() ?: 0,
                            reps = reps.toIntOrNull() ?: 0,
                            instructions = instructions,
                            requiredEquipment = equipmentList,
                            geoTag = geoTag
                        )
                    } else {
                        // Create new exercise
                        Exercise(
                            id = exerciseId ?: "",
                            name = name,
                            sets = sets.toIntOrNull() ?: 0,
                            reps = reps.toIntOrNull() ?: 0,
                            instructions = instructions,
                            requiredEquipment = equipmentList,
                            geoTag = geoTag
                        )
                    }
                    
                    if (isEditMode) {
                        exerciseViewModel.updateExercise(exercise)
                    } else {
                        exerciseViewModel.addExercise(exercise)
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
                    Text(if (isEditMode) "Update Exercise" else "Add Exercise")
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
