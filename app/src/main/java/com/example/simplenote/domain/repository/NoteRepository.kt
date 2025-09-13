package com.example.simplenote.domain.repository

import com.example.simplenote.domain.model.Note
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
}
