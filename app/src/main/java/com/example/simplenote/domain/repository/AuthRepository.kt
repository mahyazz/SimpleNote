package com.example.simplenote.domain.repository

import com.example.simplenote.data.api.model.UserInfoDto

sealed class AuthResult {
    data class Success(val message: String? = null): AuthResult()
    data class Error(val message: String): AuthResult()
}

interface AuthRepository {
    suspend fun login(username: String, password: String, scheme: String = "JWT"): AuthResult
    suspend fun register(username: String, password: String, email: String, firstName: String? = null, lastName: String? = null, scheme: String = "JWT"): AuthResult
    suspend fun refresh(): AuthResult
    fun logout()
    fun isLoggedIn(): Boolean
    fun currentScheme(): String
    fun setScheme(s: String)
    fun accessToken(): String?
    fun refreshToken(): String?
    suspend fun userInfo(): Result<UserInfoDto>
    suspend fun changePassword(old: String, new: String): AuthResult
}
