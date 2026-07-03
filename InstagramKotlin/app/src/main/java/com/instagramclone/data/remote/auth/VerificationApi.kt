package com.instagramclone.data.remote.auth

import com.instagramclone.core.network.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VerificationApi {
    @POST("v1/verification/send-code")
    suspend fun sendCode(
        @Body request: VerifyEmailRequestDto,
    ): Response<ApiEnvelope<SendCodeResponseDto>>

    @GET("v1/verification/verify")
    suspend fun verifyCode(
        @Query("otp") otp: String,
        @Query("email") email: String,
    ): Response<ApiEnvelope<Boolean>>
}
