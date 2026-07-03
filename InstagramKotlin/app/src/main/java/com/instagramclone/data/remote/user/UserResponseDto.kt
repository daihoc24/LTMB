package com.instagramclone.data.remote.user

import com.instagramclone.core.session.CurrentUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponseDto(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val avatar: String? = null,
    val privacy: Boolean? = null,
    val status: Int? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
    val birthday: String? = null,
)

fun UserResponseDto.toCurrentUser(): CurrentUser? {
    val safeId = id?.takeIf(String::isNotBlank) ?: return null
    val safeUsername = username?.takeIf(String::isNotBlank) ?: return null
    return CurrentUser(
        id = safeId,
        username = safeUsername,
        email = email,
        avatarUrl = avatar,
        isPrivate = privacy ?: false,
        status = status ?: 0,
    )
}
