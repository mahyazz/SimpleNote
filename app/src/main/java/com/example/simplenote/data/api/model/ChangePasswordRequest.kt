package com.example.simplenote.data.api.model

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)