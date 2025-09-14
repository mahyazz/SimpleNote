package com.example.simplenote.ui.auth.register

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : ComponentActivity() {

    private val viewModel: RegisterViewModel by viewModels()

    sealed class RegisterUiState {
        object Idle : RegisterUiState()
        object Loading : RegisterUiState()
        data class Success(val message: String) : RegisterUiState()
        data class Error(val message: String) : RegisterUiState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val busy by viewModel.busy.collectAsState()
            val message by viewModel.message.collectAsState()
            val success by viewModel.success.collectAsState()

            val uiState: RegisterUiState = when {
                busy -> RegisterUiState.Loading
                !busy && success && message != null -> RegisterUiState.Success(message!!)
                !busy && !success && message != null -> RegisterUiState.Error(message!!)
                else -> RegisterUiState.Idle
            }

            var firstName by remember { mutableStateOf("") }
            var lastName by remember { mutableStateOf("") }
            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var retypePassword by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }

            RegisterScreen(
                uiState = uiState,
                firstName = firstName,
                onFirstNameChange = { firstName = it },
                lastName = lastName,
                onLastNameChange = { lastName = it },
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                retypePassword = retypePassword,
                onRetypePasswordChange = { retypePassword = it },
                email = email,
                onEmailChange = { email = it },
                onBack = {finish()},
                onSubmit =  {
                    viewModel.register(username, password, email, firstName, lastName)
                }
            )
        }
    }
}
