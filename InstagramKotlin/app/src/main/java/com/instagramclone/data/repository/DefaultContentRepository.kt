package com.instagramclone.data.repository

import android.content.Context
import com.instagramclone.BuildConfig
import com.instagramclone.core.network.ApiErrorMapper
import com.instagramclone.core.network.ApiResult
import com.instagramclone.core.network.SafeErrorContext
import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import com.instagramclone.data.remote.content.ContentApi
import com.instagramclone.data.remote.content.CreatePostRequestDto
import com.instagramclone.data.remote.content.PostUserRefDto
import com.instagramclone.data.remote.content.UpdatePostRequestDto
import com.instagramclone.feature.content.ContentRepository
import com.instagramclone.feature.content.ContentResult
import com.instagramclone.feature.content.FeedPost
import com.instagramclone.feature.media.model.LocalMedia
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

@Singleton
class DefaultContentRepository @Inject constructor(
    private val api: ContentApi,
    private val errors: ApiErrorMapper,
    private val session: SessionRepository,
    @ApplicationContext private val context: Context,
) : ContentRepository {
    override suspend fun loadFeed(): ContentResult<List<FeedPost>> = safe {
        val postResult = errors.fromResponse(api.feed(), SafeErrorContext("POST", "v1/post/findAllPost"))
        if (postResult !is ApiResult.Success) return@safe ContentResult.Failure(postResult.message())
        val posts = postResult.value.filter { it.visible && it.id != null && it.user?.id != null }
            .sortedByDescending { it.createdAt.orEmpty() }
        ContentResult.Success(posts.map { post ->
            val owner = post.user!!
            FeedPost(post.id!!, owner.id!!, post.caption.orEmpty(), owner.username ?: "Người dùng", owner.avatar, post.createdAt, emptyList())
        })
    }

    override suspend fun loadMedia(posts: List<FeedPost>): ContentResult<Map<Int, List<String>>> = safe {
        if (posts.isEmpty()) return@safe ContentResult.Success(emptyMap())
        val folders = posts.map { "posts/${it.userId}/${it.id}" }
        when (val result = errors.fromResponse(api.media(folders), SafeErrorContext("POST", "v1/cloudinary/getAllMultiple"))) {
            is ApiResult.Success -> ContentResult.Success(
                posts.mapIndexed { index, post -> post.id to result.value.getOrNull(index).orEmpty() }.toMap(),
            )
            else -> ContentResult.Failure(result.message())
        }
    }

    override suspend fun loadMedia(post: FeedPost): ContentResult<List<String>> = safe {
        val folder = "posts/${post.userId}/${post.id}"
        when (val result = errors.fromResponse(api.mediaForPost(folder), SafeErrorContext("GET", "v1/cloudinary/getAll"))) {
            is ApiResult.Success -> ContentResult.Success(result.value)
            else -> ContentResult.Failure(result.message())
        }
    }

    override suspend fun createPost(caption: String, media: List<LocalMedia>): ContentResult<Unit> = safe {
        val user = (session.state.value as? SessionState.Authenticated)?.user
            ?: return@safe ContentResult.Failure("Phiên đăng nhập không hợp lệ")
        val created = errors.fromResponse(
            api.create(CreatePostRequestDto(caption.trim(), PostUserRefDto(user.id))),
            SafeErrorContext("POST", "v1/post/add"),
        )
        if (created !is ApiResult.Success || created.value.id == null) return@safe ContentResult.Failure(created.message())
        if (media.isNotEmpty()) {
            val parts = media.mapIndexed { index, item ->
                val body = ContentUriRequestBody(context, item)
                MultipartBody.Part.createFormData("fileUpload", item.displayName ?: "image_$index.jpg", body)
            }
            val response = api.upload(parts, user.id, created.value.id)
            if (!response.isSuccessful || response.body()?.code != 200) {
                return@safe ContentResult.Failure("Bài viết đã tạo nhưng tải ảnh thất bại")
            }
        }
        ContentResult.Success(Unit)
    }

    override suspend fun updateCaption(postId: Int, caption: String): ContentResult<Unit> = safe {
        val response = api.updateCaption(UpdatePostRequestDto(postId, caption = caption.trim()))
        if (response.isSuccessful && response.body()?.code in listOf(null, 200)) ContentResult.Success(Unit)
        else ContentResult.Failure("Không cập nhật được bài viết")
    }

    override suspend fun hidePost(postId: Int): ContentResult<Unit> = safe {
        val response = api.updateVisible(UpdatePostRequestDto(postId, visible = false))
        if (response.isSuccessful && response.body()?.code in listOf(null, 200)) ContentResult.Success(Unit)
        else ContentResult.Failure("Không ẩn được bài viết")
    }

    private suspend fun <T> safe(block: suspend () -> ContentResult<T>): ContentResult<T> = try { block() } catch (throwable: Throwable) {
        ContentResult.Failure(
            if (BuildConfig.DEBUG) "${throwable.javaClass.simpleName}: ${throwable.message ?: "unknown"}"
            else "Không thể kết nối máy chủ. Vui lòng thử lại.",
        )
    }

    private fun ApiResult<*>.message(): String = when (this) {
        is ApiResult.BusinessError -> userMessage
        is ApiResult.ClientError -> userMessage
        is ApiResult.DataError -> userMessage
        is ApiResult.NetworkError -> userMessage
        is ApiResult.ServerError -> userMessage
        is ApiResult.Unauthorized -> userMessage
        is ApiResult.UncategorizedError -> userMessage
        is ApiResult.Success -> "Dữ liệu không hợp lệ"
    }

    private class ContentUriRequestBody(private val context: Context, private val media: LocalMedia) : RequestBody() {
        override fun contentType() = media.mimeType.toMediaTypeOrNull()
        override fun contentLength(): Long = media.sizeBytes ?: -1
        override fun writeTo(sink: BufferedSink) {
            val input = context.contentResolver.openInputStream(media.uri) ?: throw IOException("Cannot read media")
            input.source().use { sink.writeAll(it) }
        }
    }
}
