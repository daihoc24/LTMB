package com.instagramclone.core.session

data class CurrentUser(
    val id: String,
    val username: String,
    val email: String?,
    val avatarUrl: String?,
    val isPrivate: Boolean,
    val status: Int,
)
