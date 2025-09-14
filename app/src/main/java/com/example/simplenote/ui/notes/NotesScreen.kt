@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.simplenote.ui.notes

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import com.example.simplenote.domain.model.*
import com.example.simplenote.ui.notes.add.AddNoteActivity
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.simplenote.ui.theme.*
import com.example.simplenote.R
import com.example.simplenote.ui.components.AppIcons
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import com.example.simplenote.ui.auth.login.LoginActivity
import com.example.simplenote.ui.components.AppButton

@Composable
fun NotesRoute(
    onOpenSettings: () -> Unit,
    vm: NotesViewModel = hiltViewModel()
) {

    val paging: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<Note>> = vm.paged
    val notes: androidx.paging.compose.LazyPagingItems<Note> = paging.collectAsLazyPagingItems()
    val currentFilter by vm.filter.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
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
        bottomBar = {
            Box(modifier = Modifier.background(Color.White)) {
                BottomAppBar(
                    modifier = Modifier
                        .height(96.dp)
                        .padding(16.dp),
                    containerColor = Color.White,
                    actions = {
                        IconButton(onClick = { /* Home: could navigate to root; currently no-op */ }) {
                            Icon(painter = painterResource(AppIcons.Home),
                                contentDescription = "Home",
                                tint = Purple,
                                modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Purple, CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        ctx.startActivity(Intent(ctx, AddNoteActivity::class.java))
                                    },
                                    onLongClick = {
                                        val items = listOf(
                                            NoteInput(title = "Sample note 1", description = "Created via bulk"),
                                            NoteInput(title = "Sample note 2", description = "Created via bulk"),
                                            NoteInput(title = "Sample note 3", description = "Created via bulk")
                                        )
                                        vm.addBulk(items)
                                    }
                                )
                        ) {
                            Icon(painter = painterResource(AppIcons.Add),
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(32.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onOpenSettings) {
                            Icon(painter = painterResource(AppIcons.Settings),
                                contentDescription = "Settings",
                                tint = Purple,
                                modifier = Modifier.size(32.dp))
                        }
                    },
                )
            }
        }
    ) { padding ->
        key(currentFilter){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(LightPurple)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        placeholder = { Text("Search notes", modifier = Modifier.height(44.dp)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                vm.applyFilter(
                                    title = searchQuery.text,
                                    description = null,
                                    updatedGteMillis = null,
                                    updatedLteMillis = null
                                )
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Purple,
                            focusedPlaceholderColor = Gray,
                            unfocusedPlaceholderColor = Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { vm.sync(); notes.refresh() }) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync")
                    }
                }

                val isEmpty = notes.itemCount == 0 && notes.loadState.refresh is LoadState.NotLoading

                if (isEmpty) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.getstarted),
                            contentDescription = "Get Started",
                            modifier = Modifier.size(280.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Start Your Journey",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(280.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Every big step starts with a small one. Note your first idea and start your journey!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(280.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Arrow",
                            modifier = Modifier.size(160.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Refresh state (full span)
//                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
//                            when (val s = notes.loadState.refresh) {
//                                is LoadState.Loading -> LoadingRow(text = "Loading...")
//                                is LoadState.Error -> ErrorRow(text = s.error.message ?: "Error")
//                                else -> {}
//                            }
//                        }

                        items(
                            count = notes.itemCount,
                            key = { index -> notes.peek(index)?.id ?: index }
                        ) { index ->
                            val note: Note? = notes[index]
                            if (note != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clickable {
                                            val intent = Intent(ctx, com.example.simplenote.ui.notes.detail.NoteDetailActivity::class.java).apply {
                                                putExtra("localId", note.id)
                                                note.remoteId?.let { putExtra("remoteId", it) }
                                            }
                                            ctx.startActivity(intent)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Yellow),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (note.description.isNotBlank()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = note.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Append state (full span)
//                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
//                            when (val s = notes.loadState.append) {
//                                is LoadState.Loading -> LoadingRow(text = "Loading more...")
//                                is LoadState.Error -> ErrorRow(text = s.error.message ?: "Error")
//                                else -> {}
//                            }
//                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = LightPurple
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
                    Text("Filter notes", style = MaterialTheme.typography.titleMedium, color = Purple)
                    TextButton(onClick = { showFilterSheet = false }, colors = ButtonDefaults.textButtonColors(contentColor = Purple)) { Text("Close") }
                }

                OutlinedTextField(
                    value = titleQuery,
                    onValueChange = { titleQuery = it },
                    label = { Text("Title contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = Purple.copy(alpha = 0.3f),
                        focusedLabelColor = Purple,
                        cursorColor = Purple,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                OutlinedTextField(
                    value = descQuery,
                    onValueChange = { descQuery = it },
                    label = { Text("Description contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = Purple.copy(alpha = 0.3f),
                        focusedLabelColor = Purple,
                        cursorColor = Purple,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showFromPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Purple)
                    ) { Text("From: ${formatDate(fromMillis)}") }

                    OutlinedButton(
                        onClick = { showToPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Purple)
                    ) { Text("To: ${formatDate(toMillis)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            vm.applyFilter(
                                title = titleQuery.text,
                                description = descQuery.text,
                                updatedGteMillis = fromMillis,
                                updatedLteMillis = toMillis
                            )
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple, contentColor = Color.White)
                    ) { Text("Apply") }

                    OutlinedButton(
                        onClick = {
                            titleQuery = TextFieldValue("")
                            descQuery  = TextFieldValue("")
                            fromMillis = null
                            toMillis   = null
                            vm.clearFilter()
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Purple)
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
                TextButton(
                    onClick = {
                        fromMillis = dpState.selectedDateMillis
                        showFromPicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Purple)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFromPicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Purple)
                ) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = dpState,
                colors = DatePickerDefaults.colors(
                    containerColor = LightPurple,
                    headlineContentColor = Purple,
                    weekdayContentColor = Purple,
                    subheadContentColor = Purple,
                    navigationContentColor = Purple,
                    yearContentColor = Purple,
                    currentYearContentColor = Purple,
                    selectedYearContainerColor = Purple,
                    selectedYearContentColor = Color.White,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = Purple,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Purple,
                    todayDateBorderColor = Purple
                )
            )
        }
    }

    if (showToPicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = toMillis)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        toMillis = dpState.selectedDateMillis
                        showToPicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Purple)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showToPicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Purple)
                ) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = dpState,
                colors = DatePickerDefaults.colors(
                    containerColor = LightPurple,
                    headlineContentColor = Purple,
                    weekdayContentColor = Purple,
                    subheadContentColor = Purple,
                    navigationContentColor = Purple,
                    yearContentColor = Purple,
                    currentYearContentColor = Purple,
                    selectedYearContainerColor = Purple,
                    selectedYearContentColor = Color.White,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = Purple,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Purple,
                    todayDateBorderColor = Purple
                )
            )
        }
    }

}

//@Composable
//fun LoadingRow(text: String) {
//    androidx.compose.foundation.layout.Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(12.dp)
//    ) { Text(text) }
//}

//@Composable
//fun ErrorRow(text: String) {
//    androidx.compose.foundation.layout.Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(12.dp)
//    ) { Text(text, color = MaterialTheme.colorScheme.error) }
//}

// --- Previews ---
class FakeNotesViewModel : NotesViewModel() {
    override val filter: kotlinx.coroutines.flow.StateFlow<NoteFilter?> = kotlinx.coroutines.flow.MutableStateFlow(null)

    private val sampleNotes = listOf(
        Note(
            id = "1",
            remoteId = null,
            title = "Groceries",
            description = "Buy milk, eggs, and bread",
            createdAt = 0L,
            updatedAt = 0L,
            isDeleted = false
        ),
        Note(
            id = "2",
            remoteId = null,
            title = "Work",
            description = "Finish the Compose migration task",
            createdAt = 0L,
            updatedAt = 0L,
            isDeleted = false
        )
    )
    override val paged: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<Note>> =
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(sampleNotes))
}

@androidx.compose.ui.tooling.preview.Preview(name = "NotesRoute - With Notes", showBackground = true)
@Composable
fun PreviewNotesRouteWithNotes() {
    MaterialTheme {
        NotesRoute(
            onOpenSettings = {},
            vm = FakeNotesViewModel()
        )
    }
}

class EmptyNotesViewModel : NotesViewModel() {
    override val filter: kotlinx.coroutines.flow.StateFlow<NoteFilter?> = kotlinx.coroutines.flow.MutableStateFlow(null)
    override val paged: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<Note>> =
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty())
}

@androidx.compose.ui.tooling.preview.Preview(name = "NotesRoute - Empty", showBackground = true)
@Composable
fun PreviewNotesRouteEmpty() {
    MaterialTheme {
        NotesRoute(
            onOpenSettings = {},
            vm = EmptyNotesViewModel()
        )
    }
}

