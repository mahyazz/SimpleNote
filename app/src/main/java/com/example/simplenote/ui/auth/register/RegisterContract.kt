package com.example.simplenote.ui.auth.register

sealed interface RegisterUiState {
    object Idle : RegisterUiState
    object Loading : RegisterUiState
    data class Success(val username: String) : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}
