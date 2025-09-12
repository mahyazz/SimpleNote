package com.example.simplenote.ui.auth.register

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.simplenote.ui.theme.SimpleNoteTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleNoteTheme {
                RegisterScreen(
                    onBack = { finish() },
                    onSuccess = { finish() } // یا ناوبری به صفحه‌ی بعدی
                )
            }
        }
    }
}
