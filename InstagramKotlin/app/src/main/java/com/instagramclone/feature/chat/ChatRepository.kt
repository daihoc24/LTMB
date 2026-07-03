package com.instagramclone.feature.chat

import android.net.Uri
import com.instagramclone.data.remote.chat.ChatMessageDto
import com.instagramclone.data.remote.chat.ChatUserDto
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun currentUserId(): String
    suspend fun contacts(): Result<List<ChatUserDto>>
    suspend fun history(otherId: String): Result<List<ChatMessageDto>>
    fun signals(): Flow<Unit>
    fun connect()
    fun connection(): Flow<Boolean>
    fun send(otherId: String, content: String): Boolean
    suspend fun sendImage(otherId: String, uri: Uri): Result<Unit>
    fun close()
}
