package com.example.simplenote.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import com.example.simplenote.domain.model.Result
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.data.api.model.ChangePasswordRequest

import javax.inject.Inject
import java.io.IOException
import com.example.simplenote.data.api.model.RegisterRequest
import com.example.simplenote.data.api.model.RegisterResponse
import retrofit2.Response
import com.example.simplenote.data.api.ApiService


class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val encryptedPrefs: EncryptedSharedPreferences
) : AuthRepository {

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(oldPassword, newPassword))

            android.util.Log.d("AuthRepository", "HTTP ${response.code()}: ${response.body()} / ${response.errorBody()?.string()}")

            when {
                response.isSuccessful -> {
                    Result(success = true, message = "Password changed successfully.")
                }
                response.code() == 401 -> {
                    Result(success = false, message = "Your current password is incorrect.")
                }
                response.code() == 500 -> {
                    Result(success = false, message = "A server error occurred.")
                }
                else -> {
                    Result(success = false, message = "An unexpected error occurred.")
                }
            }

        } catch (e: IOException) {
            Result(success = false, message = "Network error: Check your connection")
        } catch (e: Exception) {
            Result(success = false, message = "An unexpected error occurred.")
        }
    }

    override suspend fun logout() {
        encryptedPrefs.edit().remove("token_key").apply()
    }
    override suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        return apiService.register(request)
    }


}
