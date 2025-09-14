package com.example.simplenote.ui.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.example.simplenote.ui.settings.SettingsActivity
import com.example.simplenote.ui.auth.register.RegisterActivity
import androidx.compose.material3.MaterialTheme

@AndroidEntryPoint
class NotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                NotesRoute(
                    onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
                )
            }
        }
    }
}
