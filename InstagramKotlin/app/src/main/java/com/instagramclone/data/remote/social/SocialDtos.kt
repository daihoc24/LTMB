package com.instagramclone.data.remote.social

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class IdRefDto(val id: String)
@JsonClass(generateAdapter = true) data class PostIdDto(val id: Int)
@JsonClass(generateAdapter = true) data class LikeRequestDto(val user: IdRefDto, val post: PostIdDto)
@JsonClass(generateAdapter = true) data class FollowRequestDto(val followerId: String, val followingId: String)
@JsonClass(generateAdapter = true) data class UsernameRequestDto(val username: String)
@JsonClass(generateAdapter = true) data class UpdateProfileDto(val id: String, val username: String, val privacy: Boolean)
@JsonClass(generateAdapter = true) data class UpdateProfileResponseDto(val username: String? = null, val privacy: Boolean = false)

@JsonClass(generateAdapter = true)
data class CommentRequestDto(val postId: Int, val userId: String, val content: String, val preComment: Int? = null)

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: Int,
    val postId: Int? = null,
    val userId: String? = null,
    val content: String = "",
    val username: String? = null,
    val avatar: String? = null,
    val visible: Boolean = true,
    val createdAt: String? = null,
    val preComment: Int = 0,
)
