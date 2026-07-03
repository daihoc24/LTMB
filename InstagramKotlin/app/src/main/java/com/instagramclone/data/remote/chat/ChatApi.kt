package com.instagramclone.data.remote.chat

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ChatApi {
    @GET("messages/contacts") suspend fun contacts(@Query("userId") userId: String): Response<List<ChatUserDto>>
    @GET("messages/history") suspend fun history(@Query("userIdSend") from: String, @Query("userIdTo") to: String): Response<List<ChatMessageDto>>
}
