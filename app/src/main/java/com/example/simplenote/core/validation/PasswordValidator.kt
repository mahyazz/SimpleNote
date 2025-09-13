package com.example.simplenote.core.validation

import javax.inject.Inject

data class ValidationResult(val success: Boolean, val message: String = "OK")

class PasswordValidator @Inject constructor() {

    private val minLength = 8
    private val complexity = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")

    fun requireStrong(password: String): ValidationResult {
        if (password.length < minLength) {
            return ValidationResult(false, "Password must be at least $minLength characters.")
        }
        if (!complexity.containsMatchIn(password)) {
            return ValidationResult(false, "Use upper/lowercase letters and a digit.")
        }
        return ValidationResult(true)
    }
}
