package com.instagramclone.data.repository

import android.content.Context
import android.net.Uri
import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import com.instagramclone.data.remote.social.*
import com.instagramclone.feature.social.*
import com.instagramclone.data.remote.content.ContentApi
import com.instagramclone.data.remote.user.UserApi
import com.instagramclone.feature.content.FeedPost
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

@Singleton
class DefaultSocialRepository @Inject constructor(
    private val api: SocialApi,
    private val comments: CommentApi,
    private val session: SessionRepository,
    private val users: UserApi,
    private val content: ContentApi,
    @ApplicationContext private val context: Context,
) : SocialRepository {
    private fun me() = (session.state.value as? SessionState.Authenticated)?.user
        ?: error("Phiên đăng nhập không hợp lệ")
    override fun currentUserId(): String = me().id

    override suspend fun searchUsers(query: String): Result<List<UserResult>> = safe {
        val me = me()
        val users = api.searchUsers(query.trim(), me.id).body()?.result.orEmpty()
        users.mapNotNull { user ->
            val id = user.id ?: return@mapNotNull null
            UserResult(id, user.username ?: "Người dùng", user.avatar, api.isFollowing(FollowRequestDto(me.id, id)).body() == true)
        }
    }

    override suspend fun searchPosts(query: String): Result<List<PostResult>> = safe {
        api.searchPosts(query.trim()).body()?.result.orEmpty().mapNotNull {
            val id = it.id ?: return@mapNotNull null
            PostResult(id, it.user?.username ?: "Người dùng", it.caption.orEmpty())
        }
    }

    override suspend fun toggleFollow(user: UserResult): Result<Boolean> = safe {
        val response = api.toggleFollow(FollowRequestDto(me().id, user.id))
        check(response.isSuccessful)
        response.body() ?: !user.following
    }

    override suspend fun profile(userId: String?): Result<ProfileSummary> = safe {
        val me = me()
        val target = if (userId == null || userId == me.id) null else users.getUser(userId).body()?.result
        val id = target?.id ?: me.id
        val username = target?.username ?: me.username
        val req = UsernameRequestDto(username)
        val followed = if (id == me.id) false else api.isFollowing(FollowRequestDto(me.id, id)).body() == true
        ProfileSummary(
            id, username, target?.email ?: me.email, target?.avatar ?: me.avatarUrl,
            target?.privacy ?: me.isPrivate,
            api.followers(req).body()?.result ?: 0,
            api.following(req).body()?.result ?: 0,
            followed,
        )
    }

    override suspend fun userPosts(username: String): Result<List<FeedPost>> = safe {
        val posts = api.postsOfUser(UsernameRequestDto(username)).body()?.result.orEmpty()
            .filter { it.visible && it.id != null && it.user?.id != null }
            .sortedByDescending { it.createdAt.orEmpty() }
        coroutineScope { posts.map { post -> async {
                val owner = post.user!!
                val folder = "posts/${owner.id}/${post.id}"
                FeedPost(
                    post.id!!, owner.id!!, post.caption.orEmpty(), owner.username ?: username,
                    owner.avatar, post.createdAt,
                    content.mediaForPost(folder).body()?.result.orEmpty(),
                )
            } }.awaitAll() }
    }

    override suspend fun updateProfile(username: String, privacy: Boolean): Result<Unit> = safe {
        val response = api.updateProfile(UpdateProfileDto(me().id, username.trim(), privacy))
        check(response.isSuccessful && response.body()?.result != null)
    }

    override suspend fun updateAvatar(uri: Uri, username: String): Result<String> = safe {
        val type = context.contentResolver.getType(uri) ?: "image/jpeg"
        val file = MultipartBody.Part.createFormData("file", "avatar.jpg", UriRequestBody(context, uri, type))
        val response = users.updateAvatar(file, username.toRequestBody("text/plain".toMediaType()))
        check(response.isSuccessful) { response.errorBody()?.string() ?: "Không cập nhật được ảnh đại diện" }
        response.body()?.string()?.trim()?.takeIf { it.isNotBlank() }
            ?: error("Máy chủ không trả về địa chỉ ảnh")
    }

    override suspend fun social(postId: Int): Result<PostSocial> = safe {
        val like = LikeRequestDto(IdRefDto(me().id), PostIdDto(postId))
        PostSocial(
            liked = api.isLiked(like).body()?.result ?: false,
            likes = api.likeCount(PostIdDto(postId)).body()?.result ?: 0,
            comments = comments.list(postId).body().orEmpty().filter { it.visible },
        )
    }

    override suspend fun toggleLike(postId: Int, currentlyLiked: Boolean): Result<Unit> = safe {
        val body = LikeRequestDto(IdRefDto(me().id), PostIdDto(postId))
        val response = if (currentlyLiked) api.unlike(body) else api.like(body)
        check(response.isSuccessful)
    }

    override suspend fun addComment(postId: Int, content: String, preComment: Int?): Result<Unit> = safe {
        val value = content.trim(); require(value.length in 1..500) { "Bình luận phải từ 1 đến 500 ký tự" }
        check(comments.create(CommentRequestDto(postId, me().id, value, preComment)).isSuccessful)
    }

    override suspend fun deleteComment(id: Int): Result<Unit> = safe { check(comments.delete(id).isSuccessful) }

    private class UriRequestBody(
        private val context: Context,
        private val uri: Uri,
        private val mimeType: String,
    ) : RequestBody() {
        override fun contentType() = mimeType.toMediaTypeOrNull()
        override fun contentLength(): Long = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1
        override fun writeTo(sink: BufferedSink) {
            val input = context.contentResolver.openInputStream(uri) ?: throw IOException("Không đọc được ảnh")
            input.source().use { sink.writeAll(it) }
        }
    }

    private suspend fun <T> safe(block: suspend () -> T): Result<T> = try { Result.success(block()) }
    catch (e: Throwable) { Result.failure(IllegalStateException(e.message ?: "Không thể kết nối máy chủ")) }
}
