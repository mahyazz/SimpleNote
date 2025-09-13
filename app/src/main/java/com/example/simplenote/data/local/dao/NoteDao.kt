package com.example.simplenote.data.local.dao

import androidx.room.*
import com.example.simplenote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<NoteEntity>)

    @Query("UPDATE notes SET isDeleted = 1, dirty = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("SELECT * FROM notes WHERE dirty = 1")
    suspend fun getDirtyNotes(): List<NoteEntity>

    @Query("UPDATE notes SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)

    @Query("SELECT id FROM notes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findLocalIdByRemoteId(remoteId: Int): String?
}
