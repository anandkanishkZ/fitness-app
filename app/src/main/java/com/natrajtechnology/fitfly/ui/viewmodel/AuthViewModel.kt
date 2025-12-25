package com.natrajtechnology.fitfly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.natrajtechnology.fitfly.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations
 */
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _profileUiState = MutableStateFlow<UiState>(UiState.Initial)
    val profileUiState: StateFlow<UiState> = _profileUiState.asStateFlow()

    private val _passwordUiState = MutableStateFlow<UiState>(UiState.Initial)
    val passwordUiState: StateFlow<UiState> = _passwordUiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _currentUser.value = authRepository.currentUser
        if (authRepository.isUserLoggedIn()) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun register(email: String, password: String, displayName: String) {
        if (!validateInputs(email, password, displayName)) {
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(email, password, displayName)
            
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, password)
            
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Initial
    }

    fun updateDisplayName(displayName: String) {
        if (displayName.isBlank()) {
            _profileUiState.value = UiState.Error("Name cannot be empty")
            return
        }

        viewModelScope.launch {
            _profileUiState.value = UiState.Loading
            val result = authRepository.updateDisplayName(displayName)

            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _profileUiState.value = UiState.Success("Profile updated")
                },
                onFailure = { error ->
                    _profileUiState.value = UiState.Error(error.message ?: "Profile update failed")
                }
            )
        }
    }

    fun clearProfileUiState() {
        _profileUiState.value = UiState.Initial
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        when {
            currentPassword.isBlank() -> {
                _passwordUiState.value = UiState.Error("Current password cannot be empty")
                return
            }
            newPassword.isBlank() -> {
                _passwordUiState.value = UiState.Error("New password cannot be empty")
                return
            }
            newPassword.length < 6 -> {
                _passwordUiState.value = UiState.Error("Password must be at least 6 characters")
                return
            }
            newPassword != confirmPassword -> {
                _passwordUiState.value = UiState.Error("Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _passwordUiState.value = UiState.Loading
            val result = authRepository.changePassword(currentPassword, newPassword)

            result.fold(
                onSuccess = {
                    _passwordUiState.value = UiState.Success("Password updated")
                },
                onFailure = { error ->
                    _passwordUiState.value = UiState.Error(error.message ?: "Password update failed")
                }
            )
        }
    }

    fun clearPasswordUiState() {
        _passwordUiState.value = UiState.Initial
    }

    fun clearError() {
        _authState.value = AuthState.Initial
    }

    private fun validateInputs(email: String, password: String, displayName: String): Boolean {
        when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email cannot be empty")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = AuthState.Error("Invalid email format")
                return false
            }
            password.isBlank() -> {
                _authState.value = AuthState.Error("Password cannot be empty")
                return false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters")
                return false
            }
            displayName.isBlank() -> {
                _authState.value = AuthState.Error("Name cannot be empty")
                return false
            }
        }
        return true
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
