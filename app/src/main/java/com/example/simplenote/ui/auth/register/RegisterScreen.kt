package com.example.simplenote.ui.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.ui.components.TopBar

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: RegisterViewModel = hiltViewModel()
) {
    val state = vm.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TopBar(title = "Register", onBack = onBack)

        Spacer(Modifier.height(16.dp))

        if (state is RegisterUiState.Error) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(vm.firstName, { vm.firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vm.lastName,  { vm.lastName  = it }, label = { Text("Last Name")  }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vm.username,  { vm.username  = it }, label = { Text("Username")   }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vm.email,     { vm.email     = it }, label = { Text("Email")      }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vm.password,  { vm.password  = it }, label = { Text("Password")   },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(vm.confirm,   { vm.confirm   = it }, label = { Text("Retype Password") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.register() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RegisterUiState.Loading
        ) {
            if (state is RegisterUiState.Loading) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("Register")
            }
        }

        // هدایت بعد از موفقیت
        if (state is RegisterUiState.Success) {
            // می‌تونی اینو ببری به LoginActivity یا Home
            LaunchedEffect(Unit) { onSuccess() }
        }
    }
}
