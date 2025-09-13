package com.example.simplenote.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.simplenote.ui.theme.Purple
import androidx.compose.foundation.BorderStroke

@Composable
fun AppButton (
    text: String,
    padding: Dp,
    onClick: (() -> Unit),
    hasIcon: Boolean,
    type: String = "Filled",
) {
    val buttonColors = if (type == "Filled") {
        ButtonDefaults.buttonColors(containerColor = Purple)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    val border = if (type == "Outlined") BorderStroke(2.dp, Purple) else null
    val contentColor = if (type == "Filled") Color.White else Purple

    val content: @Composable RowScope.() -> Unit = {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = contentColor,
            modifier = Modifier.padding(padding)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (hasIcon) {
            Image(
                painter = painterResource(id = AppIcons.ArrowForward),
                contentDescription = "Icon",
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(contentColor)
            )
        }
    }

    if (type == "Outlined") {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(100.dp),
            border = border,
            colors = buttonColors,
            content = content
        )
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(100.dp),
            colors = buttonColors,
            content = content
        )
    }
}