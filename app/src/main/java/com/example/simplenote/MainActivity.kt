package com.example.simplenote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.simplenote.ui.notes.NotesRoute
import com.example.simplenote.ui.settings.SettingsActivity
import com.example.simplenote.ui.auth.register.RegisterActivity
import com.example.simplenote.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.ui.auth.OnboardingActivity
import com.example.simplenote.ui.auth.OnboardingScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!authRepo.isLoggedIn()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                NotesRoute(
                    onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
                )
            }
        }
    }
}
