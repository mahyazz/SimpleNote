package com.example.simplenote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simplenote.data.local.entity.NoteRemoteKeys


@Dao
interface NoteRemoteKeysDao {
    @Query("SELECT * FROM note_remote_keys WHERE noteId = :id")
    suspend fun remoteKeysById(id: String): NoteRemoteKeys?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<NoteRemoteKeys>)

    @Query("DELETE FROM note_remote_keys")
    suspend fun clear()
}
