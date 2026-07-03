package com.instagramclone.data.remote.content

import com.instagramclone.core.network.ApiEnvelope
import com.instagramclone.core.network.AuthRequired
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Tag

interface ContentApi {
    @POST("v1/post/findAllPost")
    suspend fun feed(@Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<List<PostDto>>>

    @POST("v1/cloudinary/getAllMultiple")
    suspend fun media(@Body folders: List<String>, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<List<List<String>>>>

    @GET("v1/cloudinary/getAll")
    suspend fun mediaForPost(@Query("folder") folder: String, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<List<String>>>

    @POST("v1/post/add")
    suspend fun create(@Body request: CreatePostRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<PostDto>>

    @POST("v1/post/updateCaption")
    suspend fun updateCaption(@Body request: UpdatePostRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<PostDto>>

    @POST("v1/post/updateVisible")
    suspend fun updateVisible(@Body request: UpdatePostRequestDto, @Tag auth: AuthRequired = AuthRequired): Response<ApiEnvelope<PostDto>>

    @Multipart
    @POST("v1/cloudinary/multiple")
    suspend fun upload(
        @Part files: List<MultipartBody.Part>,
        @Query("userId") userId: String,
        @Query("postId") postId: Int,
        @Tag auth: AuthRequired = AuthRequired,
    ): Response<ApiEnvelope<String>>

    @Multipart
    @POST("v1/cloudinary/chat")
    suspend fun uploadChatImage(
        @Part file: MultipartBody.Part,
        @Query("senderId") senderId: String,
        @Query("receiverId") receiverId: String,
        @Tag auth: AuthRequired = AuthRequired,
    ): Response<ApiEnvelope<String>>
}
