package com.example.simplenote

import com.example.simplenote.ui.components.TopBar
import com.example.simplenote.ui.components.AppLabel
import com.example.simplenote.ui.components.AppIcons
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(
                name = "Taha Hamifar",
                email = "hamifar.taha@gmail.com",
                onBack = { /* Navigate back */ },
                onChangePassword = { /* Navigate to Change Password */ },
                onLogout = { /* Handle logout */ }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    name: String,
    email: String,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar("Settings", onBack= onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            ProfileSection(
            imageRes = R.drawable.ic_pfp,
            name = name,
            email = email
            )
            HorizontalDivider(thickness = 1.dp, color = LightGray)
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
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
                        onLogout,
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
}

@Composable
fun ProfileSection(imageRes: Int, name: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Profile",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
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
fun SettingsScreenPreview() {
    SettingsScreen(
        name = "Taha Hamifar",
        email = "hamifar.taha@gmail.com",
        onBack = {},
        onChangePassword = {},
        onLogout = {}
    )
}
