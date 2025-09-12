package com.example.simplenote.ui.settings.changepassword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: ChangePasswordViewModel = hiltViewModel()
            val uiState = viewModel.uiState

            ChangePasswordScreen(
                uiState = uiState,
                currentPassword = viewModel.currentPassword,
                onCurrentPasswordChange = { viewModel.currentPassword = it },
                newPassword = viewModel.newPassword,
                onNewPasswordChange = { viewModel.newPassword = it },
                retypePassword = viewModel.retypePassword,
                onRetypePasswordChange = { viewModel.retypePassword = it },
                onBack = { finish() },
                onSubmit = { viewModel.changePassword() }
            )
        }
    }
}
