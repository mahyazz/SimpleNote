package com.example.simplenote.ui.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.domain.model.Note
import com.example.simplenote.ui.components.AppIcons
import com.example.simplenote.ui.theme.*
import androidx.compose.material3.IconButton

@Composable
fun NotesScreen(
    onSettings: () -> Unit,
    notes: List<Note>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPurple)
            .verticalScroll(rememberScrollState())
    ) {
        if (notes.isEmpty()){

        } else {

        }
        BottomBar()
    }
}

@Composable
fun BottomBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(56.dp)
        ) {
            Image(
                painter = painterResource(id = AppIcons.Home),
                contentDescription = "Home",
                modifier = Modifier
                    .size(40.dp),
                colorFilter = ColorFilter.tint(Purple)
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .size(56.dp)
                .background(Purple, CircleShape)
        ) {
            Image(
                painter = painterResource(id = AppIcons.Add),
                contentDescription = "Add",
                modifier = Modifier
                    .size(40.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .size(56.dp)
        ) {
            Image(
                painter = painterResource(id = AppIcons.Settings),
                contentDescription = "Settings",
                modifier = Modifier
                    .size(40.dp),
                colorFilter = ColorFilter.tint(Purple)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesEmptyStatePreview() {
        NotesScreen(
            onSettings = {},
            notes = emptyList()
        )
}
