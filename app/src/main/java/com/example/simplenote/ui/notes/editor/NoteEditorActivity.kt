@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.notes.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import com.example.simplenote.ui.components.AppIcons
import com.example.simplenote.ui.components.TopBar
import androidx.compose.ui.tooling.preview.Preview
import com.example.simplenote.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.material3.rememberModalBottomSheetState

@AndroidEntryPoint
class NoteEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteId = intent.getIntExtra("remoteId", -1)
        val localId = intent.getStringExtra("localId")
        setContent {
            MaterialTheme {
                NoteEditorRoute(remoteId = remoteId, localId = localId, onBack = { finish() })
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val df = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return df.format(java.util.Date(millis))
}

@Composable
fun NoteEditorRoute(
    remoteId: Int,
    localId: String?,
    onBack: () -> Unit,
    vm: NoteEditorViewModel = hiltViewModel()
) {
    val title by vm.title.collectAsState()
    val description by vm.description.collectAsState()
    val busy by vm.busy.collectAsState()
    val lastSavedAt by vm.lastSavedAt.collectAsState()
    val deleted by vm.deleted.collectAsState()

    LaunchedEffect(remoteId, localId) { vm.init(localId, remoteId) }
    LaunchedEffect(deleted) { if (deleted) onBack() }

    NoteEditorScreen(
        title = title,
        description = description,
        busy = busy,
        lastSavedAt = lastSavedAt,
        onBack = { vm.saveNow(onBack) },
        onUpdateTitle = vm::updateTitle,
        onUpdateDescription = vm::updateDescription,
        onDelete = vm::delete
    )
}

@Composable
fun NoteEditorScreen(
    title: String,
    description: String,
    busy: Boolean,
    lastSavedAt: Long?,
    onBack: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val isPreview = remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopBar(
                onBack = onBack,
                actions = {
                    androidx.compose.material3.TextButton(onClick = { isPreview.value = !isPreview.value }) {
                        Text(if (isPreview.value) "Edit" else "Preview")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(LightGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val text = if (lastSavedAt != null) "Last edited on " + formatTimestamp(lastSavedAt) else ""
                    if (text.isNotEmpty()) {
                        Text(
                            text = text,
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF6A1B9A), shape = RectangleShape)
                            .clickable(enabled = !busy) { showDeleteSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Delete),
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isPreview.value) {
                val scroll = androidx.compose.foundation.rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    Text(
                        text = title.ifBlank { "(No title)" },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = description.ifBlank { "" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = onUpdateTitle,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier,
                        decorationBox = { inner ->
                            Box {
                                if (title.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        style = TextStyle(
                                            color = Color.Black,
                                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                inner()
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    BasicTextField(
                        value = description,
                        onValueChange = onUpdateDescription,
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        ),
                        modifier = Modifier
                            .weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Feel free to write here...",
                                        style = TextStyle(
                                            color = Color.Black,
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                        )
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
            }
            if (busy) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showDeleteSheet) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text("Want to delete this note?", style = MaterialTheme.typography.titleMedium)
                androidx.compose.material3.Button(
                    onClick = {
                        showDeleteSheet = false
                        onDelete()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete Note") }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NoteEditorPreview() {
    MaterialTheme {
        NoteEditorScreen(
            title = "Grocery List",
            description = "- Apples\n- Bananas\n- Milk\n\nDon't forget to check discounts.",
            busy = false,
            lastSavedAt = System.currentTimeMillis(),
            onBack = {},
            onUpdateTitle = {},
            onUpdateDescription = {},
            onDelete = {}
        )
    }
}
