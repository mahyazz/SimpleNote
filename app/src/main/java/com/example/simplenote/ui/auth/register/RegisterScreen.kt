@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterRoute(
    onDone: () -> Unit,
    vm: RegisterViewModel
) {
    val busy    by vm.busy.collectAsState()
    val msg     by vm.message.collectAsState()
    val success by vm.success.collectAsState()
    val scheme  by vm.scheme.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it) } }
    LaunchedEffect(success) { if (success) onDone() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Register") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RegisterForm(
                busy = busy,
                currentScheme = scheme,
                onSchemeChange = vm::updateScheme,
                onSubmit = { u, p, e, f, l -> vm.register(u, p, e, f, l) }
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
private fun RegisterForm(
    busy: Boolean,
    currentScheme: String,
    onSchemeChange: (String) -> Unit,
    onSubmit: (String, String, String, String?, String?) -> Unit // ← فیکس: فلش و Unit
) {
    var username by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var first    by remember { mutableStateOf("") }
    var last     by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = email,    onValueChange = { email = it },       label = { Text("Email")    }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = password, onValueChange = { password = it },    label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
    OutlinedTextField(value = first,    onValueChange = { first = it },       label = { Text("First name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = last,     onValueChange = { last = it },        label = { Text("Last name (optional)")  }, singleLine = true, modifier = Modifier.fillMaxWidth())

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = currentScheme, onValueChange = {},
            label = { Text("Auth Scheme") }, readOnly = true,
            modifier = Modifier
                .menuAnchor() // نسخه قدیمی (سازگار با متریال فعلی پروژه‌ات)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("JWT") }, onClick = { onSchemeChange("JWT");  expanded = false })
            DropdownMenuItem(text = { Text("Bearer") }, onClick = { onSchemeChange("Bearer"); expanded = false })
        }
    }

    Button(
        onClick = {
            onSubmit(
                username.trim(),
                password,
                email.trim(),
                first.ifBlank { null },
                last.ifBlank  { null }
            )
        },
        enabled = !busy && username.isNotBlank() && password.isNotBlank() && email.isNotBlank()
    ) { Text("Register") }
}
