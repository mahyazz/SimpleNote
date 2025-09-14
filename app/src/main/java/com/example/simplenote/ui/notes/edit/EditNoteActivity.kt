@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.notes.edit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditNoteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteId = intent.getIntExtra("remoteId", -1)
        val localId = intent.getStringExtra("localId")
        setContent {
            MaterialTheme {
                EditNoteRoute(
                    remoteId = remoteId,
                    localId = localId,
                    onDone = {
                        val intent = Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun EditNoteRoute(
    remoteId: Int,
    localId: String?,
    onDone: () -> Unit,
    vm: EditNoteViewModel = hiltViewModel()
) {
    val title by vm.title.collectAsState()
    val description by vm.description.collectAsState()
    val busy by vm.busy.collectAsState()
    val done by vm.done.collectAsState()

    LaunchedEffect(remoteId, localId) { vm.init(localId, remoteId) }
    LaunchedEffect(done) { if (done) onDone() }

    Scaffold(topBar = { TopAppBar(title = { Text("Edit Note") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = vm::updateTitle,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = vm::updateDescription,
                label = { Text("Description") },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 12.dp)
            )
            Button(
                onClick = vm::save,
                enabled = !busy && title.isNotBlank() && description.isNotBlank()
            ) { Text("Save") }
        }
    }
}
