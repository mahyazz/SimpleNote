package com.example.simplenote.ui.notes.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class EditNoteViewModel @Inject constructor(
    private val repo: NoteRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done.asStateFlow()

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
                    if (n != null) {
                        _title.value = n.title
                        _description.value = n.description
                    }
                } else if (remoteId > 0) {
                    val nLocal = withContext(Dispatchers.IO) { repo.getLocalByRemoteId(remoteId) }
                    if (nLocal != null) {
                        this@EditNoteViewModel.localId = nLocal.id
                        _title.value = nLocal.title
                        _description.value = nLocal.description
                    }
                }
                if (remoteId > 0) {
                    runCatching { withContext(Dispatchers.IO) { repo.fetchRemote(remoteId) } }
                        .onSuccess {
                            this@EditNoteViewModel.localId = it.id
                            _title.value = it.title
                            _description.value = it.description
                        }
                }
            } finally {
                _busy.value = false
            }
        }
    }

    fun updateTitle(v: String) { _title.value = v }
    fun updateDescription(v: String) { _description.value = v }

    fun save() = viewModelScope.launch {
        _busy.value = true
        try {
            val t = _title.value.trim()
            val d = _description.value.trim()
            if (remoteId > 0) {
                val ok = runCatching {
                    withContext(Dispatchers.IO) { repo.patchRemote(remoteId, t, d) }
                }.isSuccess
                if (!ok) {
                    val id = localId ?: return@launch
                    withContext(Dispatchers.IO) { repo.patchLocal(id, t, d) }
                }
            } else {
                val id = localId ?: return@launch
                withContext(Dispatchers.IO) { repo.patchLocal(id, t, d) }
            }
            _done.value = true
        } finally {
            _busy.value = false
        }
    }

}
