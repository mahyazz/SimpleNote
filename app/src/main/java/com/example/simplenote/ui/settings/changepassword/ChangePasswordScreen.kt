package com.example.simplenote.ui.settings.changepassword

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

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    retypePassword: String,
    onRetypePasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar("Change Password", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Please input your current password first.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Purple,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            AppInput(
                label = "Current Password",
                placeholder = "********",
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                isPassword = true
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = LightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Now, create your new password.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Purple,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            AppInput(
                label = "New Password",
                placeholder = "********",
                value = newPassword,
                onValueChange = onNewPasswordChange,
                guide = "Password should contain a-z, A-Z, 0-9.",
                isPassword = true
            )

            Spacer(Modifier.height(8.dp))

            AppInput(
                label = "Retype New Password",
                placeholder = "********",
                value = retypePassword,
                onValueChange = onRetypePasswordChange,
                isPassword = true
            )

            Spacer(modifier = Modifier.weight(1f))


            when (uiState) {
                ChangePasswordUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is ChangePasswordUiState.Error -> {
                    LaunchedEffect(uiState) {
                        val msg = UserMessages.friendlyError(uiState.message)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    AppButton(
                        text = "Submit New Password",
                        12.dp,
                        onClick = onSubmit,
                        hasIcon = true
                    )
                }
                is ChangePasswordUiState.Success -> {
                    LaunchedEffect(uiState) {
                        val msg = UserMessages.friendlySuccess(uiState.message, fallback = "Password changed successfully.")
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
                ChangePasswordUiState.Idle -> {
                    AppButton(
                        text = "Submit New Password",
                        12.dp,
                        onClick = onSubmit,
                        hasIcon = true
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordPreview() {
    var current by remember { mutableStateOf("oldpass") }
    var new by remember { mutableStateOf("newpass") }
    var retype by remember { mutableStateOf("newpass") }

    ChangePasswordScreen(
        uiState = ChangePasswordUiState.Idle,
        currentPassword = current,
        onCurrentPasswordChange = { current = it },
        newPassword = new,
        onNewPasswordChange = { new = it },
        retypePassword = retype,
        onRetypePasswordChange = { retype = it },
        onBack = {},
        onSubmit = {}
    )
}