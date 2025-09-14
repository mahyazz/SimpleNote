package com.example.simplenote.domain.usecase

import com.example.simplenote.core.validation.PasswordValidator
import com.example.simplenote.domain.model.Result
import com.example.simplenote.domain.repository.AuthRepository
import com.example.simplenote.domain.repository.AuthResult
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repo: AuthRepository,
    private val validator: PasswordValidator
) {
    suspend operator fun invoke(
        oldPassword: String,
        newPassword: String,
        confirm: String
    ): AuthResult {
        // 1) Validate inputs (throw Exception on issues)
        if (oldPassword.isBlank() || newPassword.isBlank() || confirm.isBlank())
            throw IllegalArgumentException("All fields are required")

        if (newPassword != confirm)
            throw IllegalArgumentException("Passwords do not match")

        val v = validator.requireStrong(newPassword)
        if (!v.success)
            throw IllegalArgumentException(v.message ?: "Weak password")

        // 2) Return the repository result (Result<Unit>)
        return repo.changePassword(oldPassword, newPassword)
    }
}
