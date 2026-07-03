package com.instagramclone.data.repository

import android.content.Context
import android.net.Uri
import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import com.instagramclone.data.remote.chat.ChatApi
import com.instagramclone.data.remote.chat.ChatMessageDto
import com.instagramclone.data.remote.chat.ChatUserDto
import com.instagramclone.data.remote.chat.SendMessageDto
import com.instagramclone.data.remote.content.ContentApi
import com.instagramclone.feature.chat.ChatRepository
import com.instagramclone.feature.chat.SockJsStompClient
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

@Singleton
class DefaultChatRepository @Inject constructor(
    private val api: ChatApi,
    private val stomp: SockJsStompClient,
    private val session: SessionRepository,
    private val content: ContentApi,
    @ApplicationContext private val context: Context,
) : ChatRepository {
    private val signal = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    private val connected = MutableStateFlow(false)

    override fun currentUserId() = (session.state.value as? SessionState.Authenticated)?.user?.id.orEmpty()
    override suspend fun contacts(): Result<List<ChatUserDto>> = safe {
        api.contacts(currentUserId()).body().orEmpty().filter { it.id != null }
    }
    override suspend fun history(otherId: String): Result<List<ChatMessageDto>> = safe {
        api.history(currentUserId(), otherId).body().orEmpty()
    }
    override fun signals(): Flow<Unit> = signal
    override fun connection(): Flow<Boolean> = connected
    override fun connect() = stomp.connect({ signal.tryEmit(Unit) }, { connected.value = it })
    override fun send(otherId: String, content: String): Boolean =
        stomp.send(SendMessageDto(currentUserId(), otherId, content.trim()))

    override suspend fun sendImage(otherId: String, uri: Uri): Result<Unit> = safe {
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val part = MultipartBody.Part.createFormData(
            "fileUpload",
            "chat-image.jpg",
            UriRequestBody(context, uri, mime),
        )
        val response = content.uploadChatImage(part, currentUserId(), otherId)
        val url = response.body()?.result?.takeIf { response.isSuccessful && it.isNotBlank() }
            ?: error(response.body()?.message ?: "Không tải được ảnh")
        check(send(otherId, IMAGE_PREFIX + url)) { "Chat chưa kết nối" }
    }

    override fun close() = stomp.close()

    private suspend fun <T> safe(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: Throwable) {
        Result.failure(IllegalStateException(error.message ?: "Không thể kết nối chat server"))
    }

    private class UriRequestBody(
        private val context: Context,
        private val uri: Uri,
        private val mime: String,
    ) : RequestBody() {
        override fun contentType() = mime.toMediaTypeOrNull()
        override fun contentLength() = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1
        override fun writeTo(sink: BufferedSink) {
            val input = context.contentResolver.openInputStream(uri) ?: throw IOException("Không đọc được ảnh")
            input.source().use { sink.writeAll(it) }
        }
    }

    private companion object { const val IMAGE_PREFIX = "__IMAGE__:" }
}
