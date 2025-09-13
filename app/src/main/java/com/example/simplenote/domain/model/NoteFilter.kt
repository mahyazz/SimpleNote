package com.example.simplenote.domain.model

data class NoteFilter(
    val title: String? = null,
    val description: String? = null,
    val updatedGteMillis: Long? = null,
    val updatedLteMillis: Long? = null
)
