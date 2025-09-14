package com.example.simplenote.ui.settings

import android.content.Intent
import com.example.simplenote.ui.components.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.simplenote.ui.theme.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.simplenote.ui.settings.changepassword.*
import com.example.simplenote.R

@dagger.hilt.android.AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                SettingsScreen(
                    name = vm.name,
                    email = vm.email,
                    onBack = { finish() },
                    onChangePassword = {
                        val intent = Intent(this, ChangePasswordActivity::class.java)
                        startActivity(intent)
                    },
                    onLogout = {
                        vm.logout(
                            onSuccess = {
                                startActivity(Intent(this, com.example.simplenote.ui.auth.OnboardingActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                                finish()
                            },
                            onError = { }
                        )
                    }
                )
            }
        }
    }
}
