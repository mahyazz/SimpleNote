package com.example.simplenote.domain.repository

import com.example.simplenote.domain.model.Result

interface AuthRepository {
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}