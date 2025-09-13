package com.example.simplenote.domain.repository

import com.example.simplenote.domain.model.Result

import com.example.simplenote.data.api.model.RegisterRequest
import com.example.simplenote.data.api.model.RegisterResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun logout()
    suspend fun register(request: RegisterRequest): Response<RegisterResponse>
}