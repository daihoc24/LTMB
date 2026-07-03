package com.instagramclone.data.remote.auth

import com.instagramclone.core.network.ApiEnvelope
import com.instagramclone.data.remote.user.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistrationApi {
    @POST("v1/users")
    suspend fun createUser(
        @Body request: UserCreateRequestDto,
    ): Response<ApiEnvelope<UserResponseDto>>
}
