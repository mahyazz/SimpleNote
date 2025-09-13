@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginRoute(
    onDone: () -> Unit,
    onOpenRegister: () -> Unit,
    vm: LoginViewModel
) {
    val busy    by vm.busy.collectAsState()
    val msg     by vm.message.collectAsState()
    val success by vm.success.collectAsState()
    val scheme  by vm.scheme.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(msg)     { msg?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(success) { if (success) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                actions = { TextButton(onClick = onOpenRegister) { Text("Register") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoginForm(
                busy = busy,
                currentScheme = scheme,
                onSchemeChange = vm::updateScheme,
                onSubmit = { u, p -> vm.login(u, p) },
                onOpenRegister = onOpenRegister // ← اینجا پاس می‌دهیم
            )

            if (!msg.isNullOrBlank()) {
                Text(
                    text = msg!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    busy: Boolean,
    currentScheme: String,
    onSchemeChange: (String) -> Unit,
    onSubmit: (String, String) -> Unit,
    onOpenRegister: () -> Unit // ← پارامتر اضافه شد
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

    OutlinedTextField(
        value = username, onValueChange = { username = it },
        label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = password, onValueChange = { password = it },
        label = { Text("Password") }, singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = currentScheme, onValueChange = {},
            label = { Text("Auth Scheme") }, readOnly = true,
            modifier = Modifier
                .menuAnchor() // نسخه قدیمی (سازگار با متریال فعلی پروژه)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("JWT") }, onClick = { onSchemeChange("JWT");  expanded = false })
            DropdownMenuItem(text = { Text("Bearer") }, onClick = { onSchemeChange("Bearer"); expanded = false })
        }
    }

    Button(
        onClick = { onSubmit(username.trim(), password) },
        enabled = !busy && username.isNotBlank() && password.isNotBlank()
    ) { Text("Login") }

    // دکمهٔ ثبت‌نام برای کاربرانی که اکانت ندارند
    TextButton(onClick = onOpenRegister) {
        Text("حساب ندارید؟ ثبت‌نام کنید")
    }
}
