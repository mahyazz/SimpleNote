package com.example.simplenote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,        // locally generated UUID
    val remoteId: Int? = null,         // id سرور؛ برای رکوردهای جدید ابتدا null است
    val title: String,
    val description: String,
    val createdAt: Long,               // epoch millis
    val updatedAt: Long,               // برای حل کانفلیکت/فیلتر
    val isDeleted: Boolean = false,    // حذف نرم (برای push بعدی)
    val dirty: Boolean = false         // نیاز به sync
)

@Entity(tableName = "note_remote_keys")
data class NoteRemoteKeys(
    @PrimaryKey val noteId: String, // local id (UUID)
    val prevKey: Int?,              // شماره صفحه قبلی
    val nextKey: Int?               // شماره صفحه بعدی
)
