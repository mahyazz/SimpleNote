package com.example.simplenote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.simplenote.data.local.dao.NoteDao
import com.example.simplenote.data.local.dao.NoteRemoteKeysDao
import com.example.simplenote.data.local.entity.NoteEntity
import com.example.simplenote.data.local.entity.NoteRemoteKeys

@Database(
    entities = [NoteEntity::class, NoteRemoteKeys::class],
    version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun noteRemoteKeysDao(): NoteRemoteKeysDao
}
