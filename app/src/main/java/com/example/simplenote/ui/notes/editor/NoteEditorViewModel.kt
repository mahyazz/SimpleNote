package com.example.simplenote.ui.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repo: NoteRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _lastSavedAt = MutableStateFlow<Long?>(null)
    val lastSavedAt: StateFlow<Long?> = _lastSavedAt.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private var remoteId: Int = -1
    private var localId: String? = null

    fun init(localId: String?, remoteId: Int) {
        if (this.localId == localId && this.remoteId == remoteId) return
        this.localId = localId
        this.remoteId = remoteId
        viewModelScope.launch {
            _busy.value = true
            try {
                if (!localId.isNullOrBlank()) {
                    val n = withContext(Dispatchers.IO) { repo.getLocalByLocalId(localId) }
                    if (n != null) applyNote(n)
                } else if (remoteId > 0) {
                    val nLocal = withContext(Dispatchers.IO) { repo.getLocalByRemoteId(remoteId) }
                    if (nLocal != null) applyNote(nLocal)
                }
                if (remoteId > 0) {
                    runCatching { withContext(Dispatchers.IO) { repo.fetchRemote(remoteId) } }
                        .onSuccess { applyNote(it) }
                }
            } finally {
                _busy.value = false
            }
        }
        startAutoSave()
    }

    fun updateTitle(v: String) { _title.value = v }
    fun updateDescription(v: String) { _description.value = v }

    private fun applyNote(n: Note) {
        this.localId = n.id
        this.remoteId = n.remoteId ?: this.remoteId
        _title.value = n.title
        _description.value = n.description
    }

    @OptIn(FlowPreview::class)
    private fun startAutoSave() {
        viewModelScope.launch {
            combine(_title, _description) { t, d -> t to d }
                .debounce(500)
                .map { (t, d) -> t.trim() to d.trim() }
                .distinctUntilChanged()
                .filter { (t, d) -> t.isNotBlank() || d.isNotBlank() }
                .collect { (t, d) ->
                    saveInternal(t, d)
                }
        }
    }

    private suspend fun saveInternal(t: String, d: String) {
        _busy.value = true
        try {
            if (localId.isNullOrBlank() && remoteId <= 0) {
                val created = runCatching {
                    withContext(Dispatchers.IO) { repo.createRemote(t, d) }
                }.getOrElse {
                    withContext(Dispatchers.IO) { repo.createLocal(t, d) }
                }
                applyNote(created)
            } else if (remoteId > 0) {
                val patched = runCatching {
                    withContext(Dispatchers.IO) { repo.patchRemote(remoteId, t, d) }
                }.getOrNull()
                if (patched != null) {
                    applyNote(patched)
                } else {
                    val id = localId ?: return
                    withContext(Dispatchers.IO) { repo.patchLocal(id, t, d) }
                }
            } else {
                val id = localId ?: return
                withContext(Dispatchers.IO) { repo.patchLocal(id, t, d) }
            }
            _lastSavedAt.value = System.currentTimeMillis()
            _message.value = "Saved"
        } catch (e: Throwable) {
            _message.value = e.message ?: "Failed to save"
        } finally {
            _busy.value = false
        }
    }

    // Public method to force-save current content immediately (used when backing out)
    fun saveNow(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val t = title.value.trim()
            val d = description.value.trim()
            if (t.isNotBlank() || d.isNotBlank()) {
                saveInternal(t, d)
            }
            onDone?.invoke()
        }
    }

    fun delete() = viewModelScope.launch {
        _busy.value = true
        try {
            val ok = if (remoteId > 0) {
                runCatching { withContext(Dispatchers.IO) { repo.deleteRemote(remoteId) } }.getOrDefault(false)
            } else false
            if (!ok) {
                val id = localId
                if (!id.isNullOrBlank()) {
                    withContext(Dispatchers.IO) { repo.deleteLocal(id) }
                }
            }
            _deleted.value = true
        } finally {
            _busy.value = false
        }
    }
}
