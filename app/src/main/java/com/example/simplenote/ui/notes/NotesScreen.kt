@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simplenote.ui.notes

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.model.NoteInput
import com.example.simplenote.ui.notes.add.AddNoteActivity
import com.example.simplenote.ui.notes.detail.NoteDetailActivity
import com.example.simplenote.ui.notes.edit.EditNoteActivity
import kotlin.jvm.java
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextOverflow





@Composable
fun NotesRoute(
    onOpenSettings: () -> Unit,
    onOpenRegister: () -> Unit,
    vm: NotesViewModel = hiltViewModel()
) {

    val paging: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.simplenote.domain.model.Note>> = vm.paged
    val notes: androidx.paging.compose.LazyPagingItems<com.example.simplenote.domain.model.Note> = paging.collectAsLazyPagingItems()
    val currentFilter by vm.filter.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val ctx = LocalContext.current
    var titleQuery by remember { mutableStateOf(TextFieldValue("")) }
    var descQuery  by remember { mutableStateOf(TextFieldValue("")) }

    var fromMillis by remember { mutableStateOf<Long?>(null) }
    var toMillis   by remember { mutableStateOf<Long?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }

    val dateFmt = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
    fun formatDate(ms: Long?): String =
        ms?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFmt) } ?: "—"


    LaunchedEffect(currentFilter) { notes.refresh() }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Notes") },
                actions = {
                    TextButton(onClick = { notes.refresh() }) { Text("Refresh") }
                    TextButton(onClick = onOpenRegister) { Text("Register") }
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                    TextButton(onClick = {
                        vm.addBulk(
                            listOf(
                                NoteInput("Bulk A", "desc A"),
                                NoteInput("Bulk B", "desc B"),
                                NoteInput("Bulk C", "desc C")
                            )
                        )
                    }) { Text("Bulk") }
                    TextButton(onClick = { showFilterSheet = true }) { Text("Filter") }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                ctx.startActivity(Intent(ctx, AddNoteActivity::class.java))
            }) { Text("+") }
        }
    ) { padding ->
        key(currentFilter){LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp)
        ) {
            when (val s = notes.loadState.refresh) {
                is LoadState.Loading -> item { LoadingRow(text = "Loading...") }
                is LoadState.Error -> item { ErrorRow(text = s.error.message ?: "Error") }
                else -> {}
            }


            items(
                count = notes.itemCount,
                key = { index -> notes.peek(index)?.id ?: index }
            ) { index ->
                val note: com.example.simplenote.domain.model.Note? = notes[index]
                if (note != null) {
                    NoteRow(note = note) {
                        val intent = Intent(ctx, com.example.simplenote.ui.notes.detail.NoteDetailActivity::class.java).apply {
                            putExtra("localId", note.id)
                            note.remoteId?.let { putExtra("remoteId", it) }
                        }
                        ctx.startActivity(intent)
                    }
                }
            }






            when (val s = notes.loadState.append) {
                is LoadState.Loading -> item { LoadingRow(text = "Loading more...") }
                is LoadState.Error -> item { ErrorRow(text = s.error.message ?: "Error") }
                else -> {}
            }
            if (notes.itemCount == 0 && notes.loadState.refresh is LoadState.NotLoading) {
                item { EmptyRow() }
            }
        }}
    }

    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Filter notes", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showFilterSheet = false }) { Text("Close") }
                }

                OutlinedTextField(
                    value = titleQuery,
                    onValueChange = { titleQuery = it },
                    label = { Text("Title contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descQuery,
                    onValueChange = { descQuery = it },
                    label = { Text("Description contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showFromPicker = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("From: ${formatDate(fromMillis)}") }

                    OutlinedButton(
                        onClick = { showToPicker = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("To: ${formatDate(toMillis)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val f = com.example.simplenote.domain.model.NoteFilter(
                                title = titleQuery.text.takeIf { it.isNotBlank() },
                                description = descQuery.text.takeIf { it.isNotBlank() },
                                updatedGteMillis = fromMillis,
                                updatedLteMillis = toMillis
                            )
                            vm.setFilter(f)
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Apply") }

                    OutlinedButton(
                        onClick = {
                            titleQuery = TextFieldValue("")
                            descQuery  = TextFieldValue("")
                            fromMillis = null
                            toMillis   = null
                            vm.setFilter(null)
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Clear") }
                }
            }
        }
    }

    if (showFromPicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = fromMillis)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fromMillis = dpState.selectedDateMillis
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    if (showToPicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = toMillis)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    toMillis = dpState.selectedDateMillis
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

}

@Composable
fun NoteRow(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()                 // قبلاً fillMaxSize بود → حتماً فقط عرض را پر کن
            .padding(bottom = 10.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp, max = 160.dp)   // سقف ارتفاع کارت
                .padding(12.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.description.isNotBlank()) {
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,                          // متن طولانی کوتاه می‌شود
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun LoadingRow(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) { Text(text) }
}

@Composable
fun ErrorRow(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) { Text(text, color = MaterialTheme.colorScheme.error) }
}

@Composable
fun EmptyRow() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) { Text("No notes") }
}
