package com.example.simplenote.ui.auth.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: RegisterViewModel = hiltViewModel()
            RegisterRoute(
                onDone = {
                    // بعد از ثبت‌نام موفق → برو به صفحه لاگین
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                },
                vm = vm
            )
        }
    }
}
