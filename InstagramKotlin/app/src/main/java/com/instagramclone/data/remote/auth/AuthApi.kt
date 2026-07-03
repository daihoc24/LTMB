package com.instagramclone.data.remote.auth

import com.instagramclone.core.network.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("v1/auth/token")
    suspend fun login(
        @Body request: AuthenticationRequestDto,
    ): Response<ApiEnvelope<AuthenticationResponseDto>>
}
