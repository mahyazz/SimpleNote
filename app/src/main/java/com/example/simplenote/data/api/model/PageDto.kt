package com.example.simplenote.data.api.model

import com.google.gson.annotations.SerializedName

data class PageDto<T>(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<T>
)
