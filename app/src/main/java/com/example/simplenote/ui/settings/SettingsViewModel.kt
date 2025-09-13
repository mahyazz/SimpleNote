package com.example.simplenote.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    object Ready : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState: SettingsUiState by mutableStateOf(SettingsUiState.Loading)
        private set

    var name: String by mutableStateOf("")
        private set
    var email: String by mutableStateOf("")
        private set

    init {
        loadUser()
    }

    fun loadUser() = viewModelScope.launch {
        uiState = SettingsUiState.Loading
        val res = authRepository.userInfo()
        res.onSuccess { dto ->
            name = listOfNotNull(dto.firstName, dto.lastName).joinToString(" ").ifBlank { dto.username }
            email = dto.email.orEmpty()
            uiState = SettingsUiState.Ready
        }.onFailure {
            uiState = SettingsUiState.Error(it.localizedMessage ?: "Failed to load user info")
        }
    }

    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            authRepository.logout()
            onSuccess()
        } catch (t: Throwable) {
            onError(t.localizedMessage ?: "Logout failed")
        }
    }
}
