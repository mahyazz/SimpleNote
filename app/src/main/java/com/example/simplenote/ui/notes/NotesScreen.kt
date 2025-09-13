@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.simplenote.ui.notes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.simplenote.domain.model.Note
import androidx.compose.material3.ExperimentalMaterial3Api


@Composable
fun NotesRoute(
    onOpenSettings: () -> Unit,
    onOpenRegister: () -> Unit,
    vm: NotesViewModel = hiltViewModel()
) {
    val items = vm.paged.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Notes") },
                actions = {
                    TextButton(onClick = { vm.refresh() }) { Text("Refresh") }
                    TextButton(onClick = onOpenRegister) { Text("Register") }
                    TextButton(onClick = onOpenSettings) { Text("Settings") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp)
        ) {
            when (val s = items.loadState.refresh) {
                is LoadState.Loading -> item { LoadingRow(text = "Loading...") }
                is LoadState.Error -> item { ErrorRow(text = s.error.message ?: "Error") }
                else -> {}
            }
            items(items.itemCount) { index ->
                val note = items[index]
                if (note != null) {
                    NoteRow(note = note, onClick = {})
                }
            }
            when (val s = items.loadState.append) {
                is LoadState.Loading -> item { LoadingRow(text = "Loading more...") }
                is LoadState.Error -> item { ErrorRow(text = s.error.message ?: "Error") }
                else -> {}
            }
            if (items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading) {
                item { EmptyRow() }
            }
        }
    }
}

@Composable
fun NoteRow(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(bottom = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleMedium)
            if (note.description.isNotBlank()) {
                Text(text = note.description, style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onClick, modifier = Modifier.padding(top = 8.dp)) {
                Text("Open")
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
