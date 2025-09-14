package com.example.simplenote.ui.components

import com.example.simplenote.ui.theme.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog


@Composable
fun ActionPopup(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(280.dp)
                .height(320.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight(700),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = message,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400),
                        color = DarkGray
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                 Row(
                    modifier = Modifier.weight(1f)
                ) {AppButton("Yes", 2.dp, onConfirm, false) }
                Row(
                    modifier = Modifier.weight(1f)
                ) { AppButton("Cancel", 2.dp, onCancel, false, "Outlined") }
            }
        }
    }
}
