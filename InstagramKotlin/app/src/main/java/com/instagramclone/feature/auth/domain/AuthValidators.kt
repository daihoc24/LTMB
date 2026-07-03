package com.instagramclone.feature.auth.domain

import java.time.LocalDate

object AuthValidators {
    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val usernamePattern = Regex("^[a-zA-Z0-9._-]{3,}$")

    fun email(value: String): FieldError? = when {
        value.isBlank() -> FieldError(AuthField.EMAIL, "Email is required.")
        !emailPattern.matches(value.trim()) -> FieldError(AuthField.EMAIL, "Enter a valid email address.")
        else -> null
    }

    fun password(value: String): FieldError? = when {
        value.isBlank() -> FieldError(AuthField.PASSWORD, "Password is required.")
        value.length < 6 -> FieldError(AuthField.PASSWORD, "Password must be at least 6 characters.")
        else -> null
    }

    fun otp(value: String): FieldError? =
        if (value.trim().isEmpty()) FieldError(AuthField.OTP, "Enter the verification code.") else null

    fun username(value: String): FieldError? = when {
        value.isBlank() -> FieldError(AuthField.USERNAME, "Username is required.")
        !usernamePattern.matches(value.trim()) -> FieldError(
            AuthField.USERNAME,
            "Use at least 3 letters, numbers, dots, underscores or hyphens.",
        )
        else -> null
    }

    fun birthday(value: LocalDate?, today: LocalDate = LocalDate.now()): FieldError? = when {
        value == null -> FieldError(AuthField.BIRTHDAY, "Birthday is required.")
        !value.isBefore(today) -> FieldError(AuthField.BIRTHDAY, "Enter a birthday in the past.")
        value.plusYears(16).isAfter(today) -> FieldError(AuthField.BIRTHDAY, "You must be at least 16 years old.")
        else -> null
    }
}
