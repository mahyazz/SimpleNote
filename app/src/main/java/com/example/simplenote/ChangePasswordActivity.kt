package com.example.simplenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.ui.components.*
import com.example.simplenote.ui.theme.*

class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChangePasswordScreen(
                onBack = { /* Navigate back */ },
                onSubmit = { /* Handle submit */ }
            )
        }
    }
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBar("Change Password", onBack= onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            Text(
                text = "Please input your current password first.",
                fontSize = 12.sp,
                fontWeight = FontWeight(500),
                color = Purple,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            AppInput("Current Password", "********")

            HorizontalDivider(thickness = 1.dp, color = LightGray, modifier = Modifier.padding(vertical= 8.dp))

            Text(
                text = "Now, create your new password.",
                fontSize = 12.sp,
                fontWeight = FontWeight(500),
                color = Purple,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            AppInput("New Password", "********", "Password should contain a-z, A-Z, 0-9.")

            Spacer(Modifier.width(8.dp))
            AppInput("RetypeNew Password", "********")

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Submit New Password", color = Color.White)
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = AppIcons.ArrowRight),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .size(20.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChangePasswordPreview() {
    ChangePasswordScreen(
        onBack = {},
        onSubmit = {}
    )
}
