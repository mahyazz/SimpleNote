package com.example.simplenote.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.simplenote.data.api.NotesApi
import com.example.simplenote.data.local.AppDatabase
import com.example.simplenote.data.local.entity.NoteEntity
import com.example.simplenote.data.local.entity.NoteRemoteKeys
import com.example.simplenote.domain.model.NoteFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@OptIn(ExperimentalPagingApi::class)
class NotesRemoteMediator(
    private val db: AppDatabase,
    private val api: NotesApi,
    private val pageSize: Int,
    private val filter: NoteFilter? = null
) : RemoteMediator<Int, NoteEntity>() {

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(loadType: LoadType, state: PagingState<Int, NoteEntity>): MediatorResult =
        withContext(Dispatchers.IO) {
            try {
                val page = when (loadType) {
                    LoadType.REFRESH -> 1
                    LoadType.PREPEND -> {
                        val firstId = state.firstItemOrNull()?.id ?: return@withContext MediatorResult.Success(endOfPaginationReached = true)
                        val prev = db.noteRemoteKeysDao().remoteKeysById(firstId)?.prevKey
                        prev ?: return@withContext MediatorResult.Success(endOfPaginationReached = true)
                    }
                    LoadType.APPEND -> {
                        val lastId = state.lastItemOrNull()?.id ?: return@withContext MediatorResult.Success(endOfPaginationReached = true)
                        val next = db.noteRemoteKeysDao().remoteKeysById(lastId)?.nextKey
                        next ?: return@withContext MediatorResult.Success(endOfPaginationReached = true)
                    }
                }

                val response = if (filter == null) {
                    api.listNotes(page = page, pageSize = pageSize)
                } else {
                    api.filterNotes(
                        page = page,
                        pageSize = pageSize,
                        title = filter.title,
                        description = filter.description,
                        updatedGte = filter.updatedGteMillis?.let { isoFromMillis(it) },
                        updatedLte = filter.updatedLteMillis?.let { isoFromMillis(it) }
                    )
                }

                val list = response.results
                val end = list.isEmpty() || response.next == null

                db.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        db.noteRemoteKeysDao().clear()
                    }

                    val entities = list.map { dto ->
                        val created = parseIsoToMillis(dto.createdAt)
                        val updated = parseIsoToMillis(dto.updatedAt).takeIf { it > 0 } ?: created
                        val existing = dto.id?.let { db.noteDao().getByRemoteId(it) }
                        val keepLocal = existing?.dirty == true
                        val title = if (keepLocal) existing!!.title else dto.title.orEmpty()
                        val description = if (keepLocal) existing!!.description else dto.description.orEmpty()
                        val localId = existing?.id ?: dto.id?.let { db.noteDao().findLocalIdByRemoteId(it) } ?: java.util.UUID.randomUUID().toString()
                        NoteEntity(
                            id = localId,
                            remoteId = dto.id,
                            title = title,
                            description = description,
                            createdAt = created,
                            updatedAt = updated,
                            dirty = existing?.dirty ?: false,
                            isDeleted = existing?.isDeleted ?: false
                        )
                    }
                    db.noteDao().upsertAll(entities)

                    val prevKey = if (page == 1) null else page - 1
                    val nextKey = if (end) null else page + 1
                    val keys = entities.map { NoteRemoteKeys(noteId = it.id, prevKey = prevKey, nextKey = nextKey) }
                    db.noteRemoteKeysDao().insertAll(keys)
                }

                MediatorResult.Success(endOfPaginationReached = end)
            } catch (t: Throwable) {
                MediatorResult.Error(t)
            }
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

    private fun isoFromMillis(millis: Long): String =
        java.time.Instant.ofEpochMilli(millis).atOffset(java.time.ZoneOffset.UTC).toString()

}
