package com.example.simplenote.domain.usecase

import com.example.simplenote.core.validation.PasswordValidator
import com.example.simplenote.domain.model.Result
import com.example.simplenote.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repo: AuthRepository,
    private val validator: PasswordValidator
) {
    suspend operator fun invoke(
        oldPassword: String,
        newPassword: String,
        confirm: String
    ): Result<Unit> {
        // 1) اعتبارسنجی ورودی‌ها (در صورت مشکل، Exception پرتاب کن)
        if (oldPassword.isBlank() || newPassword.isBlank() || confirm.isBlank())
            throw IllegalArgumentException("تمام فیلدها باید پر شوند")

        if (newPassword != confirm)
            throw IllegalArgumentException("رمزها مطابقت ندارند")

        val v = validator.requireStrong(newPassword)
        if (!v.success)
            throw IllegalArgumentException(v.message ?: "رمز عبور ضعیف است")

        // 2) فقط نتیجه‌ی ریپو را برگردان (از نوع Result<Unit>)
        return repo.changePassword(oldPassword, newPassword)
    }
}
