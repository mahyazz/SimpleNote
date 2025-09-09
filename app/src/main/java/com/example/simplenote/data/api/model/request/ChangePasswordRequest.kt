package com.example.simplenote.data.api.model.request

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)