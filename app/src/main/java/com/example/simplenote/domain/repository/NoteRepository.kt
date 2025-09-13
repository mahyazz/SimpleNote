package com.example.simplenote.domain.repository

import androidx.paging.PagingData
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.model.NoteInput
import kotlinx.coroutines.flow.Flow

data class RefreshResult(
    val pulled: Int,
    val pages: Int,
    val ok: Boolean,
    val message: String? = null   // ← پیام تشخیصی (کد HTTP و …)
)

data class SyncResult(
    val pushed: Int,
    val deleted: Int,
    val pulled: Int,
    val ok: Boolean,
    val message: String? = null
)

data class PingResult(
    val ok: Boolean,
    val httpCode: Int? = null,
    val httpMessage: String? = null,
    val error: String? = null
)

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun upsertLocal(note: Note)
    suspend fun deleteLocal(id: String)

    suspend fun refresh(): RefreshResult
    suspend fun sync(): SyncResult
    suspend fun ping(): PingResult
    fun pagedNotes(pageSize: Int = 20): Flow<PagingData<Note>>
    suspend fun createRemote(title: String, description: String): Note
    suspend fun fetchRemote(remoteId: Int): Note
    suspend fun updateRemote(remoteId: Int, title: String, description: String): Note
    suspend fun getLocalByRemoteId(remoteId: Int): Note?
    suspend fun updateLocal(id: String, title: String, description: String)
    suspend fun createLocal(title: String, description: String): Note
    suspend fun getLocalByLocalId(id: String): Note?
    suspend fun patchLocal(id: String, title: String?, description: String?)
    suspend fun patchRemote(remoteId: Int, title: String?, description: String?): Note
    suspend fun deleteRemote(remoteId: Int): Boolean
    suspend fun bulkCreateRemote(items: List<NoteInput>): List<Note>
    suspend fun bulkCreateLocal(items: List<NoteInput>): List<Note>
    fun pagedNotesFiltered(filter: com.example.simplenote.domain.model.NoteFilter, pageSize: Int = 20): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.simplenote.domain.model.Note>>


}
