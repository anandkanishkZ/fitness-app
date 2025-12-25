package com.natrajtechnology.fitfly.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natrajtechnology.fitfly.data.model.WorkoutRoutine
import com.natrajtechnology.fitfly.data.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing workout routines
 */
class RoutineViewModel : ViewModel() {
    private val routineRepository = RoutineRepository()

    private val _routines = MutableStateFlow<List<WorkoutRoutine>>(emptyList())
    val routines: StateFlow<List<WorkoutRoutine>> = _routines.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            try {
                Log.d("RoutineViewModel", "Starting to load routines...")
                routineRepository.getRoutines().collect { routineList ->
                    Log.d("RoutineViewModel", "Received ${routineList.size} routines")
                    _routines.value = routineList
                }
            } catch (e: Exception) {
                Log.e("RoutineViewModel", "Error loading routines", e)
                _uiState.value = UiState.Error(e.message ?: "Failed to load routines")
            }
        }
    }

    fun addRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = routineRepository.addRoutine(routine)
            
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Routine added successfully")
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to add routine")
                }
            )
        }
    }

    fun updateRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = routineRepository.updateRoutine(routine)
            
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Routine updated successfully")
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to update routine")
                }
            )
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = routineRepository.deleteRoutine(routineId)
            
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success("Routine deleted successfully")
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to delete routine")
                }
            )
        }
    }

    fun toggleRoutineCompletion(routineId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            android.util.Log.d("RoutineViewModel", "toggleRoutineCompletion called for $routineId, current state: $isCompleted")
            val result = routineRepository.toggleRoutineCompletion(routineId, isCompleted)
            
            result.fold(
                onSuccess = {
                    android.util.Log.d("RoutineViewModel", "Toggle successful for $routineId")
                    // Success, list will update automatically via Flow
                },
                onFailure = { error ->
                    android.util.Log.e("RoutineViewModel", "Toggle failed: ${error.message}")
                    _uiState.value = UiState.Error(error.message ?: "Failed to update routine")
                }
            )
        }
    }
    
    suspend fun getRoutineById(routineId: String): Result<WorkoutRoutine> {
        return routineRepository.getRoutineById(routineId)
    }

    fun clearUiState() {
        _uiState.value = UiState.Initial
    }
}
