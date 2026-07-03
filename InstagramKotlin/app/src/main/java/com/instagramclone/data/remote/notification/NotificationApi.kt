package com.instagramclone.data.remote.notification

import com.instagramclone.core.network.AuthRequired
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag

@JsonClass(generateAdapter = true)
data class NotificationDto(val avatar: String? = null, val message: String = "")

interface NotificationApi {
    @POST("v1/notification")
    suspend fun list(@Query("userId") userId: String, @Tag auth: AuthRequired = AuthRequired): Response<List<NotificationDto>>
}
