package com.example.simplenote.ui.components

import com.example.simplenote.ui.theme.AccentPurple
import com.example.simplenote.ui.theme.LightGray
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment

@Composable
fun TopBar(
    title: String? = null,
    backButtonText: String = "Back",
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLabel(backButtonText, AppIcons.ArrowLeft, 16.sp, FontWeight(500), 20.dp, onBack, AccentPurple)
        }

        title?.let {
            Text(
                text = it,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
    HorizontalDivider(thickness = 1.dp, color = LightGray)
}
