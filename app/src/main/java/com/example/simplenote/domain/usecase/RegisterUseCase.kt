package com.example.simplenote.domain.usecase

import com.example.simplenote.core.validation.PasswordValidator
import com.example.simplenote.data.api.model.RegisterRequest
import com.example.simplenote.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repo: AuthRepository,
    private val validator: PasswordValidator
) {
    /**
     * @throws IllegalArgumentException در صورت ضعف رمز یا ناهماهنگی رمزها
     */
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirm: String
    ) = run {
        if (listOf(firstName, lastName, username, email, password).any { it.isBlank() })
            throw IllegalArgumentException("تمام فیلدها باید پر شوند")

        val v = validator.requireStrong(password)
        if (!v.success) throw IllegalArgumentException(v.message)

        if (password != confirm)
            throw IllegalArgumentException("رمزها مطابقت ندارند")

        repo.register(
            RegisterRequest(
                username = username,
                password = password,
                email = email,
                first_name = firstName,
                last_name = lastName
            )
        )
    }
}
