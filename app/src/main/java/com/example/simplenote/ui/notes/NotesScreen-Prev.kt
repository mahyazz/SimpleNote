@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.notes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplenote.domain.model.Note

@Composable
fun NotesScreen(
    onOpenSettings: () -> Unit,
    vm: NotesViewModel = hiltViewModel()
) {
    val notes by vm.notes.collectAsState()
    val context = LocalContext.current
    val busy: Boolean = vm.isBusy.value
    var showAddDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }

    val msg: String? = vm.snackbarMessage.value
    LaunchedEffect(msg) {
        msg?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.consumeMessage() }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    TextButton(onClick = { /* Home - no op */ }) { Text("Home") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") }
                }
            )
        }
    ) { padding: PaddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

            // Header: search + centered title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = { Text("Search…") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            if (notes.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )
            } else {
                NotesGrid(
                    notes = notes,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                )
            }
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
                vm.applyAuthSettings(token, scheme)
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
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Illustration placeholder
            Box(
                Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) { Text("Illustration", color = MaterialTheme.colorScheme.onSurfaceVariant) }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Start Your Journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Every big step start with small step.\nNotes your first idea and start\nyour journey!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id }) { n ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(n.description, style = MaterialTheme.typography.bodySmall, maxLines = 6)
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

@Preview(showBackground = true)
@Composable
private fun NotesEmptyStatePreview() {
    MaterialTheme {
        EmptyState(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesGridPreview() {
    val sample = listOf(
        Note(
            id = "1",
            remoteId = 10,
            title = "Grocery List",
            description = "- Milk\n- Eggs\n- Bread\n- Coffee",
            createdAt = System.currentTimeMillis() - 86_400_000,
            updatedAt = System.currentTimeMillis() - 43_200_000
        ),
        Note(
            id = "2",
            remoteId = null,
            title = "App Ideas",
            description = "SimpleNote redesign with grid view and empty state.",
            createdAt = System.currentTimeMillis() - 172_800_000,
            updatedAt = System.currentTimeMillis() - 3_600_000
        ),
        Note(
            id = "3",
            remoteId = 11,
            title = "Quote",
            description = "Every big step starts with a small step.",
            createdAt = System.currentTimeMillis() - 900_000,
            updatedAt = System.currentTimeMillis() - 600_000
        ),
        Note(
            id = "4",
            remoteId = null,
            title = "Meeting Notes",
            description = "Sync flow: push, delete, pull.\nToken via JWT/Bearer.",
            createdAt = System.currentTimeMillis() - 2_700_000,
            updatedAt = System.currentTimeMillis() - 1_200_000
        )
    )

    MaterialTheme {
        NotesGrid(
            notes = sample,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
    }
}