package com.instagramclone.data.remote.user

import com.instagramclone.core.network.ApiEnvelope
import com.instagramclone.core.network.AuthRequired
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Tag

interface UserApi {
    @Multipart
    @POST("v1/users/updateAvat")
    suspend fun updateAvatar(
        @Part file: MultipartBody.Part,
        @Part("username") username: RequestBody,
        @Tag authRequired: AuthRequired = AuthRequired,
    ): Response<ResponseBody>

    @GET("v1/users/{id}")
    suspend fun getUser(
        @Path("id") id: String,
        @Tag authRequired: AuthRequired = AuthRequired,
    ): Response<ApiEnvelope<UserResponseDto>>

    @GET("v1/users/my-info")
    suspend fun getMyInfo(
        @Tag authRequired: AuthRequired = AuthRequired,
    ): Response<ApiEnvelope<UserResponseDto>>
}
