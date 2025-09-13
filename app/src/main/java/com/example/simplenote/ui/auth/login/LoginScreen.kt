package com.example.simplenote.ui.auth.login

import android.content.Intent
import android.widget.Toast
import com.example.simplenote.ui.util.UserMessages
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.simplenote.ui.auth.login.LoginActivity.LoginUiState
import com.example.simplenote.ui.auth.register.*

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center
    ) {
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
                    text = "Let's Login",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "And note your ideas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AppInput(
                label = "Username",
                placeholder = "Example: @HamifarTaha",
                value = username,
                onValueChange = onUsernameChange
            )

            AppInput(
                label = "Password",
                placeholder = "********",
                value = password,
                onValueChange = onPasswordChange,
                isPassword = true
            )

            when (uiState) {
                LoginUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is LoginUiState.Error -> {
                    LaunchedEffect(uiState) {
                        val msg = UserMessages.friendlyError(uiState.message)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    AppButton(
                        text = "Register",
                        padding = 12.dp,
                        onClick = onSubmit,
                        hasIcon = true
                    )
                }
                is LoginUiState.Success -> {
                    LaunchedEffect(uiState) {
                        val msg = UserMessages.friendlySuccess(uiState.message, fallback = "Logged in successfully.")
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                LoginUiState.Idle -> {
                    AppButton(
                        text = "Login",
                        padding = 12.dp,
                        onClick = onSubmit,
                        hasIcon = true
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Gray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "or",
                    color = DarkGray
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Gray,
                    modifier = Modifier.weight(1f)
                )
            }


            TextButton(
                onClick = {
                    val intent = Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Don't have an account? Register here.",
                    color = Purple,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    LoginScreen(
        uiState = LoginUiState.Idle,
        username = user,
        onUsernameChange = { user = it },
        password = pass,
        onPasswordChange = { pass = it },
        onSubmit = {}
    )
}
