package com.instagramclone.data.remote.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthenticationRequestDto(
    val email: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthenticationResponseDto(
    val authenticated: Boolean = false,
    val token: String? = null,
)

@JsonClass(generateAdapter = true)
data class VerifyEmailRequestDto(val email: String)

// Intentionally empty: the backend OTP field must never enter the mobile domain/UI.
@JsonClass(generateAdapter = true)
class SendCodeResponseDto

@JsonClass(generateAdapter = true)
data class UserCreateRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val birthday: String,
)
