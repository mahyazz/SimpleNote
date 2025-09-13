@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.notes.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.simplenote.ui.notes.edit.EditNoteActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.simplenote.MainActivity
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height



private val detailDateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private fun fmt(ms: Long?): String =
    ms?.takeIf { it > 0 }?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(detailDateFmt)
    } ?: "-"

@AndroidEntryPoint
class NoteDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteId = intent.getIntExtra("remoteId", -1)
        val localId = intent.getStringExtra("localId")
        setContent {
            MaterialTheme {
                NoteDetailRoute(remoteId = remoteId, localId = localId)
            }
        }
    }
}

@Composable
fun NoteDetailRoute(
    remoteId: Int,
    localId: String?,
    vm: NoteDetailViewModel = hiltViewModel()
) {
    val note by vm.note.collectAsState()
    val busy by vm.busy.collectAsState()
    val ctx = LocalContext.current
    val deleted by vm.deleted.collectAsState()
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(deleted) {
        if (deleted) {
            ctx.startActivity(
                Intent(ctx, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }


    LaunchedEffect(remoteId, localId) { vm.load(remoteId, localId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note") },
                actions = {
                    val n = note
                    TextButton(onClick = {
                        if (remoteId > 0) {
                            ctx.startActivity(
                                Intent(ctx, EditNoteActivity::class.java)
                                    .putExtra("remoteId", remoteId)
                            )
                        }
                        if (n != null) {
                            ctx.startActivity(
                                Intent(ctx, com.example.simplenote.ui.notes.edit.EditNoteActivity::class.java)
                                    .putExtra("localId", n.id)
                                    .putExtra("remoteId", n.remoteId ?: -1)
                            )
                        }

                    }) { Text("Edit") }
                    TextButton(onClick = { showDeleteSheet = true }) { Text("Delete") }


                }

            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (busy) {
                Text("Loading...")
            } else {
                Text(note?.title.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(note?.description.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))
            Text("Updated: ${fmt(note?.updatedAt)}", style = MaterialTheme.typography.labelSmall)
            Text("Created: ${fmt(note?.createdAt)}", style = MaterialTheme.typography.labelSmall)

        }
    }

    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = sheetState
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Want to delete this note?", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showDeleteSheet = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                Button(
                    onClick = {
                        showDeleteSheet = false
                        vm.delete(localId = note?.id, remoteId = note?.remoteId ?: -1)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete Note") }
            }
        }
    }

}
