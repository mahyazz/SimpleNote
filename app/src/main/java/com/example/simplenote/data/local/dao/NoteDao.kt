package com.example.simplenote.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.simplenote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ----- خواندن لیست (برای State ساده) -----
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    // ----- Paging 3 -----
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun pagingSource(): PagingSource<Int, NoteEntity>

    // ----- CRUD پایه -----
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("""
        UPDATE notes 
        SET title = :title, description = :description, dirty = 1, updatedAt = :now 
        WHERE id = :id
    """)
    suspend fun updateContent(id: String, title: String, description: String, now: Long)

    // ----- حذف نرم/سخت -----
    @Query("UPDATE notes SET isDeleted = 1, dirty = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDelete(id: String)

    // ----- سینک (dirty/remoteId) -----
    @Query("SELECT * FROM notes WHERE dirty = 1")
    suspend fun getDirtyNotes(): List<NoteEntity>

    @Query("UPDATE notes SET dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<String>)

    @Query("SELECT id FROM notes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findLocalIdByRemoteId(remoteId: Int): String?

    // ----- کمکی -----
    @Query("DELETE FROM notes")
    suspend fun clearAll()

    @Query("SELECT * FROM notes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): NoteEntity?

    @Query("""
UPDATE notes 
SET 
  title = COALESCE(:title, title), 
  description = COALESCE(:description, description), 
  dirty = 1, 
  updatedAt = :now 
WHERE id = :id
""")
    suspend fun updatePartial(id: String, title: String?, description: String?, now: Long)

    @Query("""
SELECT * FROM notes
WHERE isDeleted = 0
  AND (:title IS NULL OR title LIKE '%' || :title || '%')
  AND (:description IS NULL OR description LIKE '%' || :description || '%')
  AND (:gte IS NULL OR updatedAt >= :gte)
  AND (:lte IS NULL OR updatedAt <= :lte)
ORDER BY updatedAt DESC
""")
    fun pagingSourceFiltered(
        title: String?,
        description: String?,
        gte: Long?,
        lte: Long?
    ): androidx.paging.PagingSource<Int, com.example.simplenote.data.local.entity.NoteEntity>



}
