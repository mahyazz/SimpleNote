@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.domain.model.Note
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesRoute(
    onOpenSettings: () -> Unit,
    onOpenRegister: () -> Unit,
    vm: NotesViewModel = hiltViewModel()
) {
    val notes by vm.notes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val busy: Boolean = vm.isBusy.value
    var showAddDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }

    val msg: String? = vm.snackbarMessage.value
    LaunchedEffect(msg) {
        msg?.let { snackbarHostState.showSnackbar(it); vm.consumeMessage() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SimpleNote") },
                actions = {
                    TextButton(onClick = { showTokenDialog = true }) { Text("Token") }
                    TextButton(onClick = { vm.ping() }) { Text("Ping") }
                    TextButton(onClick = { vm.refresh() }) { Text("Refresh") }
                    TextButton(onClick = { vm.sync() }) { Text("Sync") }
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                    TextButton(onClick = onOpenRegister) { Text("Register") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") } }
    ) { padding: PaddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            NotesList(
                notes = notes,
                onDelete = { id: String -> vm.delete(id) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title: String, desc: String -> vm.add(title, desc); showAddDialog = false }
        )
    }

    if (showTokenDialog) {
        TokenDialog(
            currentStatus = vm.tokenStatus(),
            onDismiss = { showTokenDialog = false },
            onSave = { token, scheme ->
                vm.setScheme(scheme)      // اول اسکیم
                vm.setToken(token)        // بعد توکن
                showTokenDialog = false
            },
            onClear = {
                vm.clearToken()
                showTokenDialog = false
            },
            onRawTest = { vm.debugNetwork() }
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    if (notes.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("هنوز یادداشتی نیست. با دکمه + اضافه کن.")
        }
        return
    }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items = notes, key = { n: Note -> n.id }) { n: Note ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(n.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(n.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "updated: ${fmt.format(Date(n.updatedAt))} • remoteId: ${n.remoteId ?: "—"}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onDelete(n.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, minLines = 3)
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank() && desc.isNotBlank()) onConfirm(title.trim(), desc.trim()) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TokenDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onSave: (token: String, scheme: String) -> Unit,
    onClear: () -> Unit,
    onRawTest: () -> Unit
) {
    var token by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var scheme by rememberSaveable { mutableStateOf("JWT") } // پیش‌فرض

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auth Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(currentStatus, style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("JWT/Bearer token") })
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = scheme,
                        onValueChange = {},
                        label = { Text("Scheme") }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("JWT") }, onClick = { scheme = "JWT"; expanded = false })
                        DropdownMenuItem(text = { Text("Bearer") }, onClick = { scheme = "Bearer"; expanded = false })
                    }
                }
                OutlinedButton(onClick = onRawTest) { Text("RAW TEST (HTTPS)") }
            }
        },
        confirmButton = { TextButton(onClick = { if (token.isNotBlank()) onSave(token, scheme) }) { Text("Save") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onClear) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}
