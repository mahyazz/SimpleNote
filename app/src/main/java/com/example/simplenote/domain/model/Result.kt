package com.example.simplenote.domain.model

data class Result<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
