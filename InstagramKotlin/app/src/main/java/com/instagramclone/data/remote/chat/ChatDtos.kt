package com.instagramclone.data.remote.chat

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatUserDto(val id: String? = null, val username: String? = null, val avatar: String? = null)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    val id: Int? = null,
    val visible: Boolean = true,
    val content: String = "",
    val createdAt: String? = null,
    val userIdSend: String? = null,
    val userIdTo: String? = null,
    val groupChatId: Long? = null,
)

@JsonClass(generateAdapter = true)
data class SendMessageDto(val userIdSend: String, val userIdTo: String, val content: String, val type: Boolean = false)
