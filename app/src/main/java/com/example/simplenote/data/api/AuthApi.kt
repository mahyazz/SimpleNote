package com.example.simplenote.data.api

import com.example.simplenote.data.api.model.*
import retrofit2.http.*

interface AuthApi {
    @POST("/api/auth/token/")
    suspend fun createToken(@Body body: TokenRequest): TokenResponse

    @POST("/api/auth/token/refresh/")
    suspend fun refreshToken(@Body body: RefreshRequest): RefreshResponse

    @GET("/api/auth/userinfo/")
    suspend fun userInfo(): UserInfoDto

    @POST("/api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("/api/auth/change-password/")
    suspend fun changePassword(@Body body: ChangePasswordRequest): DetailResponse
}
