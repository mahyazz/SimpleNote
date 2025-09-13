package com.example.simplenote.domain.model

data class Note(
    val id: String,           // local uuid
    val remoteId: Int?,       // server id
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
