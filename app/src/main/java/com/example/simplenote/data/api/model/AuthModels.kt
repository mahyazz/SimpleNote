package com.example.simplenote.data.api.model

import com.google.gson.annotations.SerializedName

/* ----- token ----- */
data class TokenRequest(
    val username: String,
    val password: String
)
data class TokenResponse(
    val access: String,
    val refresh: String
)

/* ----- refresh ----- */
data class RefreshRequest(
    val refresh: String
)
data class RefreshResponse(
    val access: String
)

/* ----- register ----- */
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name")  val lastName: String? = null
)
data class RegisterResponse(
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name")  val lastName: String?
)

/* ----- userinfo ----- */
data class UserInfoDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name")  val lastName: String?
)

/* ----- change password ----- */
data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)
data class DetailResponse(
    val detail: String?
)
