package com.example.simplenote.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.ui.auth.login.LoginActivity
import com.example.simplenote.ui.settings.changepassword.ChangePasswordActivity
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
                    startActivity(Intent(this, ChangePasswordActivity::class.java))
                },
                onLogout = {
                    // تأیید از طریق UI خودت (دیالوگ) اگر داری؛ اینجا فقط اکشن نهایی:
                    viewModel.logout(
                        onSuccess = {
                            // ارسال به صفحه لاگین و بستن استک
                            startActivity(Intent(this, LoginActivity::class.java))
                            finishAffinity()
                        },
                        onError = { msg ->
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}
