package com.instagramclone.feature.auth.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthOperationResult
    suspend fun sendOtp(email: String): AuthOperationResult
    suspend fun verifyOtp(email: String, otp: String): AuthOperationResult
    suspend fun register(draft: RegistrationDraft): AuthOperationResult
}
