package com.instagramclone.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    val code: Int = 200,
    val message: String? = null,
    val result: T? = null,
)
