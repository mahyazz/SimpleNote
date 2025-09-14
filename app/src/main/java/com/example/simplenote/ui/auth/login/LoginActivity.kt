package com.example.simplenote.ui.auth.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.simplenote.ui.notes.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()

    sealed class LoginUiState {
        object Idle : LoginUiState()
        object Loading : LoginUiState()
        data class Success(val message: String) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val busy by viewModel.busy.collectAsState()
            val message by viewModel.message.collectAsState()
            val success by viewModel.success.collectAsState()

            val uiState: LoginUiState = when {
                busy -> LoginUiState.Loading
                !busy && success && message != null -> LoginUiState.Success(message!!)
                !busy && !success && message != null -> LoginUiState.Error(message!!)
                else -> LoginUiState.Idle
            }

            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            var navigated by remember { mutableStateOf(false) }

            // Navigate only after successful login
            if (uiState is LoginUiState.Success && !navigated) {
                navigated = true
                val intent = Intent(this, NotesActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
                finish()
            }

            LoginScreen(
                uiState = uiState,
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                onSubmit = {
                    viewModel.login(username, password)
                }
            )
        }
    }
}
