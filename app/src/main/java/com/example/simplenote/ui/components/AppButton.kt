package com.example.simplenote.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.ui.theme.Purple

@Composable
fun AppButton (
    text: String,
    padding: Dp,
    onClick: (() -> Unit),
    hasIcon: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color.White,
                modifier = Modifier.padding(padding)
            )

            Spacer(modifier = Modifier.weight(1f))
            if (hasIcon) {
                Image(
                    painter = painterResource(id = AppIcons.ArrowForward),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .size(20.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}