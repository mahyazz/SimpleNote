package com.example.simplenote.ui.notes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repo: NoteRepository
) : ViewModel() {
    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun load(remoteId: Int, localId: String?) = viewModelScope.launch {
        _busy.value = true
        try {
            if (!localId.isNullOrBlank()) {
                repo.getLocalByLocalId(localId)?.let { _note.value = it }
            } else if (remoteId > 0) {
                repo.getLocalByRemoteId(remoteId)?.let { _note.value = it }
            }
            if (remoteId > 0) {
                runCatching { repo.fetchRemote(remoteId) }
                    .onSuccess { _note.value = it }
            }
        } finally {
            _busy.value = false
        }
    }

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun delete(localId: String?, remoteId: Int) = viewModelScope.launch {
        val ok = runCatching { withContext(Dispatchers.IO) { repo.deleteRemote(remoteId) } }
            .getOrDefault(false)
        if (!ok && localId != null) {
            withContext(Dispatchers.IO) { repo.deleteLocal(localId) }
            _deleted.value = true
        } else if (ok) {
            _deleted.value = true
        }
    }

}
