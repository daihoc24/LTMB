package com.instagramclone.data.remote.social

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentApi {
    @GET("comments") suspend fun list(@Query("postId") postId: Int): Response<List<CommentDto>>
    @POST("comments") suspend fun create(@Body body: CommentRequestDto): Response<CommentDto>
    @DELETE("comments/{id}") suspend fun delete(@Path("id") id: Int): Response<Unit>
}
