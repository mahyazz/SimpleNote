package com.example.simplenote.data.mappers

import com.example.simplenote.data.api.model.NoteDto
import com.example.simplenote.data.local.entity.NoteEntity
import com.example.simplenote.domain.model.Note
import java.time.Instant

/* ----- زمان: امن ----- */
fun String.toEpochMillisSafe(): Long =
    runCatching { Instant.parse(this).toEpochMilli() }
        .getOrElse { System.currentTimeMillis() }

fun epochMillisToIsoZ(ms: Long): String = Instant.ofEpochMilli(ms).toString()

/* ----- Entity <-> Domain ----- */
fun NoteEntity.toDomain() = Note(
    id = id,
    remoteId = remoteId,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)

fun Note.toEntity(dirty: Boolean) = NoteEntity(
    id = id,
    remoteId = remoteId,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    dirty = dirty
)

/* ----- DTO -> Entity (ایمن) ----- */
fun NoteDto.toEntityOrNull(): NoteEntity? {
    val rid = id ?: return null
    val now = System.currentTimeMillis()
    val created = createdAt?.toEpochMillisSafe() ?: now
    val updated = updatedAt?.toEpochMillisSafe() ?: created
    val safeTitle = title ?: "(untitled)"
    val safeDesc = description ?: ""
    return NoteEntity(
        id = "remote-$rid",
        remoteId = rid,
        title = safeTitle,
        description = safeDesc,
        createdAt = created,
        updatedAt = updated,
        isDeleted = false,
        dirty = false
    )
}
