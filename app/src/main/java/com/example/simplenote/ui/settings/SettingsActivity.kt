package com.example.simplenote.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.simplenote.ui.settings.changepassword.*
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState = viewModel.uiState

            SettingsScreen(
                uiState = uiState,
                name = viewModel.name,
                email = viewModel.email,
                onBack = { finish() },
                onChangePassword = {
                    val intent = Intent(this, ChangePasswordActivity::class.java)
                    startActivity(intent)
                },
                onLogout = { viewModel.logout({}, {}) }
//                        TO-DO: add onSuccess, onError
            )
        }
    }
}
