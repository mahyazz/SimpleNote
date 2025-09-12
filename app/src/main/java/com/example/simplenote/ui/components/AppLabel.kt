package com.example.simplenote.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*

@Composable
fun AppLabel(
    text: String,
    icon: Int,
    textSize: TextUnit,
    textWeight: FontWeight,
    iconSize: Dp,
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Black,
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ){
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onClick?.invoke() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Icon",
                modifier = Modifier.size(iconSize),
                colorFilter = ColorFilter.tint(tint)
            )
            Text(
                text = text,
                fontSize = textSize,
                color = tint,
                fontWeight = textWeight
            )
        }
    }
}