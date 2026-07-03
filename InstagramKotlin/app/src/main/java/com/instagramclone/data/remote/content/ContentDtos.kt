package com.instagramclone.data.remote.content

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostUserDto(val id: String? = null, val username: String? = null, val avatar: String? = null)

@JsonClass(generateAdapter = true)
data class PostDto(
    val id: Int? = null,
    val visible: Boolean = true,
    val caption: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val user: PostUserDto? = null,
)

@JsonClass(generateAdapter = true)
data class PostUserRefDto(val id: String)

@JsonClass(generateAdapter = true)
data class CreatePostRequestDto(val caption: String, val user: PostUserRefDto)

@JsonClass(generateAdapter = true)
data class UpdatePostRequestDto(val id: Int, val caption: String? = null, val visible: Boolean? = null)
