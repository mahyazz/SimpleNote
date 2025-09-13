package com.example.simplenote.ui.auth.register

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.ui.components.*
import com.example.simplenote.ui.theme.*
import com.example.simplenote.ui.auth.register.RegisterActivity.RegisterUiState
import com.example.simplenote.ui.auth.login.LoginActivity

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    retypePassword: String,
    onRetypePasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current

    fun handleSignup() {
        if (password != retypePassword) {
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}\$")
        if (!passwordRegex.matches(password)) {
            Toast.makeText(
                context,
                "Password must be at least 8 characters, with uppercase, lowercase, and a number.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        onSubmit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(backButtonText = "Back to Login", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Register",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "And start taking notes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AppInput(
                label = "First Name",
                placeholder = "Example: Taha",
                value = firstName,
                onValueChange = onFirstNameChange
            )

            AppInput(
                label = "Last Name",
                placeholder = "Example: Hamifar",
                value = lastName,
                onValueChange = onLastNameChange
            )

            AppInput(
                label = "Username",
                placeholder = "Example: @HamifarTaha",
                value = username,
                onValueChange = onUsernameChange
            )

            AppInput(
                label = "Email Address",
                placeholder = "Example: hamifar.taha@gmail.com",
                value = email,
                onValueChange = onEmailChange
            )

            AppInput(
                label = "Password",
                placeholder = "********",
                value = password,
                onValueChange = onPasswordChange,
                isPassword = true
            )

            AppInput(
                label = "Retype Password",
                placeholder = "********",
                value = retypePassword,
                onValueChange = onRetypePasswordChange,
                isPassword = true
            )

            Spacer(modifier = Modifier.weight(1f))

            when (uiState) {
                RegisterUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is RegisterUiState.Error -> {
                    LaunchedEffect(uiState) {
                        Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                    }
                    AppButton(
                        text = "Register",
                        padding = 12.dp,
                        onClick = onSubmit,
                        hasIcon = true
                    )
                }
                is RegisterUiState.Success -> {
                    LaunchedEffect(uiState) {
                        Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                    }
                }
                RegisterUiState.Idle -> {
                    AppButton(
                        text = "Register",
                        padding = 12.dp,
                        onClick = {handleSignup()},
                        hasIcon = true
                    )
                }
            }

            TextButton(
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Already have an account? Login here.",
                    color = Purple,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var retype by remember { mutableStateOf("") }

    RegisterScreen(
        uiState = RegisterUiState.Idle,
        firstName = first,
        onFirstNameChange = { first = it },
        lastName = last,
        onLastNameChange = { last = it },
        username = user,
        onUsernameChange = { user = it },
        email = email,
        onEmailChange = { email = it },
        password = pass,
        onPasswordChange = { pass = it },
        retypePassword = retype,
        onRetypePasswordChange = { retype = it },
        onBack = {},
        onSubmit = {}
    )
}
