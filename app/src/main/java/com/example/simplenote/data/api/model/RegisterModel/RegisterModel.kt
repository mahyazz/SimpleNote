package com.example.simplenote.data.api.model

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val first_name: String,
    val last_name: String
)

data class RegisterResponse(
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String
)
