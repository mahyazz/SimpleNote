package com.example.simplenote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.ui.theme.Gray

@Composable
fun AppInput(
    label: String,
    placeholder: String,
    guide: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
        value = "",
        onValueChange = {},
        label = { Text(placeholder) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
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