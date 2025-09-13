package com.example.simplenote.data.api.model

import com.google.gson.annotations.SerializedName

data class NoteDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("created_at") val createdAt: String?,  // ISO8601
    @SerializedName("updated_at") val updatedAt: String?,  // ISO8601
    @SerializedName("creator_name") val creatorName: String? = null,
    @SerializedName("creator_username") val creatorUsername: String? = null
)

data class NoteCreateUpdateBody(
    val title: String,
    val description: String
)
