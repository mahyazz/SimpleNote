package com.example.simplenote.data.api.model.request

import com.google.gson.annotations.SerializedName

class ErrorResponse {
    data class ErrorResponse(
        val type: String,
        val errors: List<ApiError>
    )

    data class ApiError(
        val code: String,
        val detail: String,
        val attr: String?
    )
}