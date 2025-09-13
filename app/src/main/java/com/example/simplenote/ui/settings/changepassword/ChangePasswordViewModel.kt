package com.example.simplenote.ui.settings.changepassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Result
import com.example.simplenote.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf<ChangePasswordUiState>(ChangePasswordUiState.Idle)
        private set

    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var retypePassword by mutableStateOf("")

    fun changePassword() {
        if (newPassword != retypePassword) {
            uiState = ChangePasswordUiState.Error("New passwords do not match")
            return
        }
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,}$")
        if (!passwordRegex.matches(newPassword)) {
            uiState = ChangePasswordUiState.Error(
                "Password must be at least 8 characters and contain lowercase, uppercase, and digits."
            )
            return
        }

        viewModelScope.launch {
            uiState = ChangePasswordUiState.Loading
            val result = authRepository.changePassword(currentPassword, newPassword)
            uiState = if (result.success) {
                ChangePasswordUiState.Success(result.message)
            } else {
                ChangePasswordUiState.Error(result.message)
            }
        }

    }

    fun resetState() {
        uiState = ChangePasswordUiState.Idle
        currentPassword = ""
        newPassword = ""
        retypePassword = ""
    }
}

sealed class ChangePasswordUiState {
    object Idle : ChangePasswordUiState()
    object Loading : ChangePasswordUiState()
    data class Success(val message: String) : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}
