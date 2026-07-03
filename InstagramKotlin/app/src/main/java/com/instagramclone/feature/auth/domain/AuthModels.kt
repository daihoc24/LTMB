package com.instagramclone.feature.auth.domain

import java.time.LocalDate

enum class RegistrationStep { EMAIL, OTP, PASSWORD, BIRTHDAY, USERNAME, TERMS }

data class RegistrationDraft(
    val email: String = "",
    val otp: String = "",
    val otpVerified: Boolean = false,
    val password: String = "",
    val birthday: LocalDate? = null,
    val username: String = "",
)

enum class AuthField { EMAIL, PASSWORD, OTP, BIRTHDAY, USERNAME }

data class FieldError(
    val field: AuthField,
    val message: String,
)

sealed interface AuthOperationResult {
    data object Success : AuthOperationResult
    data class ValidationError(val errors: List<FieldError>) : AuthOperationResult
    data class Failure(val message: String, val recoverable: Boolean = true) : AuthOperationResult
}
