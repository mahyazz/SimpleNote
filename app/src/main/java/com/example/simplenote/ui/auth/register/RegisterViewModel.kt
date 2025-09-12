package com.example.simplenote.ui.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var uiState by mutableStateOf<RegisterUiState>(RegisterUiState.Idle)
        private set

    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirm by mutableStateOf("")

    fun register() {
        viewModelScope.launch {
            uiState = RegisterUiState.Loading
            try {
                val response = registerUseCase(
                    firstName, lastName, username, email, password, confirm
                )
                if (response.isSuccessful) {
                    uiState = RegisterUiState.Success(username)
                } else {
                    uiState = RegisterUiState.Error("خطا: ${response.code()}")
                }
            } catch (e: IllegalArgumentException) {
                uiState = RegisterUiState.Error(e.message ?: "ورودی نامعتبر")
            } catch (e: Exception) {
                uiState = RegisterUiState.Error(e.message ?: "خطا در ارتباط با سرور")
            }
        }
    }
}
