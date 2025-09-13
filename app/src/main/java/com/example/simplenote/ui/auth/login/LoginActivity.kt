package com.example.simplenote.ui.auth.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.MainActivity
import com.example.simplenote.ui.auth.register.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: LoginViewModel = hiltViewModel()
            LoginRoute(
                onDone = {
                    // ورود موفق → برو به خانهٔ نوت‌ها
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onOpenRegister = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                },
                vm = vm
            )
        }
    }
}
