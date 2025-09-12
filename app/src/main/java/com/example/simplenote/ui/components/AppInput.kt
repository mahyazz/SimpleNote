package com.example.simplenote.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.ui.theme.Gray

@Composable
fun AppInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    guide: String? = null,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (isPassword) {
                    val icon = if (passwordVisible) {
                        AppIcons.Visible
                    } else {
                        AppIcons.Invisible
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Image(
                            painter = if (passwordVisible) painterResource(id = AppIcons.Visible) else painterResource(id = AppIcons.Invisible),
                            contentDescription = "Toggle password visibility",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.DarkGray)
                        )
                    }
                }
            }
        )

        guide?.let {
            Text(
                text = guide,
                fontSize = 12.sp,
                fontWeight = FontWeight(400),
                color = Gray,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
