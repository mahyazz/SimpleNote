package com.example.simplenote.ui.notes.add

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
class AddNoteViewModel @Inject constructor(
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

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    fun updateTitle(v: String) { _title.value = v }
    fun updateDescription(v: String) { _description.value = v }

    fun save() = viewModelScope.launch {
        _busy.value = true
        try {
            val t = _title.value.trim()
            val d = _description.value.trim()
            val ok = runCatching {
                withContext(Dispatchers.IO) { repo.createRemote(t, d) }
            }.isSuccess
            if (!ok) {
                withContext(Dispatchers.IO) { repo.createLocal(t, d) }
            }
            _success.value = true
            _message.value = "Saved"
        } catch (e: Throwable) {
            _message.value = e.message ?: "Failed"
        } finally {
            _busy.value = false
        }
    }
}
