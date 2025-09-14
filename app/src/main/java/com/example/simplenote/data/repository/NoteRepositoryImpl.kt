package com.example.simplenote.data.repository

import com.example.simplenote.data.paging.NotesRemoteMediator
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
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.simplenote.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.simplenote.data.local.entity.NoteEntity
import com.example.simplenote.domain.repository.NoteRepository
import com.example.simplenote.data.api.model.NotePatchBody
import com.example.simplenote.domain.model.NoteInput
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import retrofit2.Response

import java.util.UUID



@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val api: NotesApi,
    private val noteDao: NoteDao,
    private val workManager: WorkManager,
    private val db: AppDatabase
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

    @OptIn(ExperimentalPagingApi::class)
    override fun pagedNotes(pageSize: Int): Flow<PagingData<Note>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            remoteMediator = NotesRemoteMediator(db, api, pageSize),
            pagingSourceFactory = { noteDao.pagingSource() }
        ).flow.map { pagingData: PagingData<NoteEntity> ->
            pagingData.map { entity: NoteEntity -> entity.toDomain() }
        }


    private fun parseIsoToMillis(s: String?): Long {
        if (s.isNullOrBlank()) return 0L
        return try {
            OffsetDateTime.parse(s).toInstant().toEpochMilli()
        } catch (_: Throwable) {
            try {
                LocalDateTime.parse(s).toInstant(ZoneOffset.UTC).toEpochMilli()
            } catch (_: Throwable) {
                0L
            }
        }
    }

    private fun NoteEntity.toDomain(): Note =
        Note(
            id = id,
            title = title,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            remoteId = remoteId
        )

    override suspend fun createRemote(title: String, description: String): Note {
        val dto = api.createNote(NoteCreateUpdateBody(title = title, description = description))
        val created = parseIsoToMillis(dto.createdAt)
        val updated = parseIsoToMillis(dto.updatedAt).takeIf { it > 0 } ?: created
        val existingLocalId = dto.id?.let { noteDao.findLocalIdByRemoteId(it) }
        val entity = NoteEntity(
            id = existingLocalId ?: UUID.randomUUID().toString(),
            remoteId = dto.id,
            title = dto.title.orEmpty(),
            description = dto.description.orEmpty(),
            createdAt = created,
            updatedAt = updated,
            isDeleted = false,
            dirty = false
        )
        noteDao.upsert(entity)
        return entity.toDomain()
    }


    override suspend fun updateRemote(remoteId: Int, title: String, description: String): Note {
        val dto = api.updateNote(remoteId, NoteCreateUpdateBody(title, description))
        val created = parseIsoToMillis(dto.createdAt)
        val updated = parseIsoToMillis(dto.updatedAt).takeIf { it > 0 } ?: created
        val localId = dto.id?.let { noteDao.findLocalIdByRemoteId(it) } ?: UUID.randomUUID().toString()
        val entity = NoteEntity(
            id = localId,
            remoteId = dto.id,
            title = dto.title.orEmpty(),
            description = dto.description.orEmpty(),
            createdAt = created,
            updatedAt = updated,
            isDeleted = false,
            dirty = false
        )
        noteDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun getLocalByRemoteId(remoteId: Int): Note? {
        val e = noteDao.getByRemoteId(remoteId) ?: return null
        return e.toDomain()
    }

    override suspend fun updateLocal(id: String, title: String, description: String) {
        val now = System.currentTimeMillis()
        noteDao.updateContent(id, title, description, now)
    }

    override suspend fun createLocal(title: String, description: String): Note {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = java.util.UUID.randomUUID().toString(),
            remoteId = null,
            title = title,
            description = description,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
            dirty = true
        )
        noteDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun getLocalByLocalId(id: String): Note? {
        val e = noteDao.getById(id) ?: return null
        return e.toDomain()
    }

    override suspend fun fetchRemote(remoteId: Int): Note {
        val dto = api.getNote(remoteId)
        val created = parseIsoToMillis(dto.createdAt)
        val updated = parseIsoToMillis(dto.updatedAt).takeIf { it > 0 } ?: created
        val existing = noteDao.getByRemoteId(remoteId)
        val keepLocal = existing?.dirty == true
        val title = if (keepLocal) existing!!.title else dto.title.orEmpty()
        val description = if (keepLocal) existing!!.description else dto.description.orEmpty()
        val localId = existing?.id ?: noteDao.findLocalIdByRemoteId(remoteId) ?: java.util.UUID.randomUUID().toString()
        val entity = NoteEntity(
            id = localId,
            remoteId = dto.id,
            title = title,
            description = description,
            createdAt = created,
            updatedAt = updated,
            isDeleted = existing?.isDeleted ?: false,
            dirty = existing?.dirty ?: false
        )
        noteDao.upsert(entity)
        return entity.toDomain()
    }
    override suspend fun patchRemote(remoteId: Int, title: String?, description: String?): Note {
        val dto = api.patchNote(remoteId, NotePatchBody(title, description))
        val created = parseIsoToMillis(dto.createdAt)
        val updated = parseIsoToMillis(dto.updatedAt).takeIf { it > 0 } ?: created
        val existing = noteDao.getByRemoteId(remoteId)
        val localId = existing?.id ?: noteDao.findLocalIdByRemoteId(remoteId) ?: java.util.UUID.randomUUID().toString()
        val entity = NoteEntity(
            id = localId,
            remoteId = dto.id,
            title = dto.title.orEmpty(),
            description = dto.description.orEmpty(),
            createdAt = created,
            updatedAt = updated,
            isDeleted = existing?.isDeleted ?: false,
            dirty = false
        )
        noteDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun patchLocal(id: String, title: String?, description: String?) {
        val now = System.currentTimeMillis()
        noteDao.updatePartial(id, title, description, now)
    }




    override suspend fun deleteRemote(remoteId: Int): Boolean {
        val resp: Response<Unit> = api.deleteNote(remoteId)
        val localId = noteDao.findLocalIdByRemoteId(remoteId) ?: noteDao.getByRemoteId(remoteId)?.id
        return if (resp.isSuccessful) {
            if (localId != null) noteDao.hardDelete(localId)
            true
        } else {
            false
        }
    }

    override suspend fun deleteLocal(id: String) {
        val now = System.currentTimeMillis()
        noteDao.softDelete(id, now)
    }
    override suspend fun bulkCreateRemote(items: List<NoteInput>): List<Note> {
        val dtos = api.createNotesBulk(items.map { NoteCreateUpdateBody(it.title, it.description) })
        val entities = dtos.map { dto ->
            val created = parseIsoToMillis(dto.createdAt)
            val updated = parseIsoToMillis(dto.updatedAt).takeIf { it > 0 } ?: created
            val localId = dto.id?.let { noteDao.findLocalIdByRemoteId(it) } ?: UUID.randomUUID().toString()
            NoteEntity(localId, dto.id, dto.title.orEmpty(), dto.description.orEmpty(), created, updated, false, false)
        }
        noteDao.upsertAll(entities)
        return entities.map { it.toDomain() }
    }

    override suspend fun bulkCreateLocal(items: List<NoteInput>): List<Note> {
        val now = System.currentTimeMillis()
        val entities = items.map {
            NoteEntity(UUID.randomUUID().toString(), null, it.title, it.description, now, now, false, true)
        }
        noteDao.upsertAll(entities)
        return entities.map { it.toDomain() }
    }

    @androidx.paging.ExperimentalPagingApi
    override fun pagedNotesFiltered(
        filter: com.example.simplenote.domain.model.NoteFilter,
        pageSize: Int
    ): kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.simplenote.domain.model.Note>> =
        androidx.paging.Pager(
            config = androidx.paging.PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            remoteMediator = com.example.simplenote.data.paging.NotesRemoteMediator(db, api, pageSize, filter),
            pagingSourceFactory = { noteDao.pagingSourceFiltered(filter.title, filter.description, filter.updatedGteMillis, filter.updatedLteMillis) }
        ).flow.map { it.map { e -> e.toDomain() } }

    override suspend fun clearLocalData() {
        // Clear all local notes and paging keys atomically
        db.withTransaction {
            db.noteRemoteKeysDao().clear()
            db.noteDao().clearAll()
        }
    }
}
