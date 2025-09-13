package com.example.simplenote.data.repository

import androidx.work.*
import com.example.simplenote.data.api.NotesApi
import com.example.simplenote.data.api.model.NoteCreateUpdateBody
import com.example.simplenote.data.local.dao.NoteDao
import com.example.simplenote.data.mappers.*
import com.example.simplenote.domain.model.Note
import com.example.simplenote.domain.repository.*
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val api: NotesApi,
    private val noteDao: NoteDao,
    private val workManager: WorkManager
) : NoteRepository {

    override fun observeNotes() =
        noteDao.observeNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertLocal(note: Note) {
        val now = System.currentTimeMillis()
        val withTs = note.copy(
            createdAt = if (note.createdAt == 0L) now else note.createdAt,
            updatedAt = max(note.updatedAt, now)
        )
        noteDao.upsert(withTs.toEntity(dirty = true))
        enqueueSync()
    }

    override suspend fun deleteLocal(id: String) {
        noteDao.softDelete(id, System.currentTimeMillis())
        enqueueSync()
    }

    override suspend fun refresh(): RefreshResult {
        val pageSize = 50
        var page: Int? = 1
        var totalPulled = 0
        var pages = 0

        // اولین درخواست را با Response می‌زنیم تا اگر خطا بود کدش را بدانیم
        val first = runCatching { api.listNotesResponse(page = 1, pageSize = pageSize) }.getOrNull()
            ?: return RefreshResult(0, 0, ok = false, message = "Network error (no response)")

        if (!first.isSuccessful) {
            return RefreshResult(0, 0, ok = false, message = "HTTP ${first.code()} ${first.message()}")
        }

        val firstBody = first.body()
            ?: return RefreshResult(0, 0, ok = false, message = "HTTP ${first.code()} empty body")

        val entsFirst = firstBody.results.mapNotNull { it.toEntityOrNull() }
        if (entsFirst.isNotEmpty()) noteDao.upsertAll(entsFirst)
        totalPulled += entsFirst.size
        pages += 1

        // بقیه صفحات (اگر next داشت)
        page = if (firstBody.next != null) 2 else null
        while (page != null) {
            val resp = runCatching { api.listNotes(page = page, pageSize = pageSize) }.getOrNull() ?: break
            val ents = resp.results.mapNotNull { it.toEntityOrNull() }
            if (ents.isNotEmpty()) noteDao.upsertAll(ents)
            totalPulled += ents.size
            pages += 1
            page = if (resp.next != null) (page + 1) else null
        }

        return RefreshResult(pulled = totalPulled, pages = pages, ok = true, message = "HTTP 200 OK")
    }

    override suspend fun sync(): SyncResult {
        var pushed = 0
        var deleted = 0

        val dirty = noteDao.getDirtyNotes()
        val cleaned = mutableListOf<String>()

        for (e in dirty) {
            if (e.isDeleted) {
                if (e.remoteId != null) runCatching { api.deleteNote(e.remoteId) }
                noteDao.hardDelete(e.id)
                cleaned += e.id
                deleted += 1
            } else {
                val body = NoteCreateUpdateBody(e.title, e.description)
                if (e.remoteId == null) {
                    val dto = runCatching { api.createNote(body) }.getOrNull()
                    if (dto != null) {
                        val created = dto.createdAt?.toEpochMillisSafe() ?: e.createdAt
                        val updated = dto.updatedAt?.toEpochMillisSafe() ?: System.currentTimeMillis()
                        noteDao.upsert(e.copy(remoteId = dto.id ?: e.remoteId, createdAt = created, updatedAt = updated, dirty = false))
                        cleaned += e.id
                        pushed += 1
                    }
                } else {
                    val dto = runCatching { api.updateNote(e.remoteId, body) }.getOrNull()
                    if (dto != null) {
                        val created = dto.createdAt?.toEpochMillisSafe() ?: e.createdAt
                        val updated = dto.updatedAt?.toEpochMillisSafe() ?: System.currentTimeMillis()
                        noteDao.upsert(e.copy(title = dto.title ?: e.title, description = dto.description ?: e.description, createdAt = created, updatedAt = updated, dirty = false))
                        cleaned += e.id
                        pushed += 1
                    }
                }
            }
        }
        if (cleaned.isNotEmpty()) noteDao.markClean(cleaned)

        val r = refresh()
        // اگر Pull هم شکست خورده بود، پیامش را پاس بده
        return SyncResult(pushed = pushed, deleted = deleted, pulled = r.pulled, ok = r.ok, message = r.message)
    }

    override suspend fun ping(): PingResult {
        val resp = runCatching { api.listNotesResponse(page = 1, pageSize = 1) }.getOrNull()
            ?: return PingResult(ok = false, error = "Network error (no response)")

        return PingResult(
            ok = resp.isSuccessful,
            httpCode = resp.code(),
            httpMessage = resp.message(),
            error = if (resp.isSuccessful) null else "HTTP ${resp.code()} ${resp.message()}"
        )
    }

    private fun enqueueSync() {
        val req = OneTimeWorkRequestBuilder<com.example.simplenote.data.sync.NotesSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork(
            com.example.simplenote.data.sync.NotesSyncWorker.NAME,
            ExistingWorkPolicy.KEEP,
            req
        )
    }
}
