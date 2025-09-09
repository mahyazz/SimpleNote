package com.example.simplenote.data.api

import com.example.simplenote.data.api.model.ChangePasswordRequest
import com.example.simplenote.data.api.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/change-password/")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

}