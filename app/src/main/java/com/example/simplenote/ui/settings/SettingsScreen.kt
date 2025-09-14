package com.example.simplenote.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.R
import com.example.simplenote.ui.components.ActionPopup
import com.example.simplenote.ui.components.AppIcons
import com.example.simplenote.ui.components.AppLabel
import com.example.simplenote.ui.components.TopBar
import com.example.simplenote.ui.theme.DarkGray
import com.example.simplenote.ui.theme.Gray
import com.example.simplenote.ui.theme.LightGray
import com.example.simplenote.ui.theme.Red

@Composable
fun SettingsScreen(
    name: String,
    email: String,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar("Settings", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileSection(
                imageRes = R.drawable.ic_pfp,
                name = name.ifBlank { "-" },
                email = email.ifBlank { "-" }
            )
            HorizontalDivider(thickness = 1.dp, color = LightGray)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "APP SETTINGS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight(400),
                    color = DarkGray
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start = 8.dp, end = 8.dp)
                        .align(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLabel(
                        "Change Password",
                        AppIcons.Lock,
                        16.sp,
                        FontWeight(500),
                        24.dp,
                        onChangePassword
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = LightGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLabel(
                        "Log Out",
                        AppIcons.Logout,
                        16.sp,
                        FontWeight(500),
                        24.dp,
                        { showDialog = true },
                        Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Taha Notes v1.1",
            fontSize = 12.sp,
            color = Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }

    if (showDialog) {
        ActionPopup(
            title = "Log Out",
            message = "Are you sure you want to log out from the application?",
            onConfirm = {
                onLogout()
                showDialog = false
            },
            onCancel = { showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ProfileSection(imageRes: Int, name: String, email: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Profile",
            modifier = Modifier
                .size(64.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = name, fontSize = 20.sp, fontWeight = FontWeight(700))
            AppLabel(email, AppIcons.Email, 12.sp, FontWeight(400), 15.dp, null, DarkGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        name = "John Doe",
        email = "john@example.com",
        onBack = {},
        onChangePassword = {},
        onLogout = {}
    )
}
