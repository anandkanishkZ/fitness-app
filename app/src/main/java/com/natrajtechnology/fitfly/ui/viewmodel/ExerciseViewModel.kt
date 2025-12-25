package com.natrajtechnology.fitfly.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natrajtechnology.fitfly.data.model.Exercise
import com.natrajtechnology.fitfly.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing exercises
 */
class ExerciseViewModel : ViewModel() {
    private val exerciseRepository = ExerciseRepository()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            try {
                Log.d("ExerciseViewModel", "Starting to load exercises...")
                exerciseRepository.getExercises().collect { exerciseList ->
                    Log.d("ExerciseViewModel", "Received ${exerciseList.size} exercises")
                    _exercises.value = exerciseList
                }
            } catch (e: Exception) {
                Log.e("ExerciseViewModel", "Error loading exercises", e)
                _uiState.value = UiState.Error(e.message ?: "Failed to load exercises")
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = exerciseRepository.addExercise(exercise)
            
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Exercise added successfully")
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to add exercise")
                }
            )
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = exerciseRepository.updateExercise(exercise)
            
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Exercise updated successfully")
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to update exercise")
                }
            )
        }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = exerciseRepository.deleteExercise(exerciseId)
            
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Exercise deleted successfully")
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to delete exercise")
                }
            )
        }
    }

    fun toggleExerciseCompletion(exerciseId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            android.util.Log.d("ExerciseViewModel", "toggleExerciseCompletion called for $exerciseId, current state: $isCompleted")
            val result = exerciseRepository.toggleExerciseCompletion(exerciseId, isCompleted)
            
            result.fold(
                onSuccess = {
                    android.util.Log.d("ExerciseViewModel", "Toggle successful for $exerciseId")
                    // Success, list will update automatically via Flow
                },
                onFailure = { error ->
                    android.util.Log.e("ExerciseViewModel", "Toggle failed: ${error.message}")
                    _uiState.value = UiState.Error(error.message ?: "Failed to update exercise")
                }
            )
        }
    }
    
    suspend fun getExerciseById(exerciseId: String): Result<Exercise> {
        return exerciseRepository.getExerciseById(exerciseId)
    }

    fun clearUiState() {
        _uiState.value = UiState.Initial
    }
}

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}
