package com.instagramclone.feature.social

import android.net.Uri
import com.instagramclone.data.remote.social.CommentDto
import com.instagramclone.feature.content.FeedPost

data class UserResult(val id: String, val username: String, val avatar: String?, val following: Boolean)
data class PostResult(val id: Int, val username: String, val caption: String)
data class ProfileSummary(val id: String, val username: String, val email: String?, val avatar: String?, val privacy: Boolean, val followers: Int, val following: Int, val followedByMe: Boolean = false)
data class PostSocial(val liked: Boolean = false, val likes: Int = 0, val comments: List<CommentDto> = emptyList(), val loading: Boolean = false, val error: String? = null)

interface SocialRepository {
    fun currentUserId(): String
    suspend fun searchUsers(query: String): Result<List<UserResult>>
    suspend fun searchPosts(query: String): Result<List<PostResult>>
    suspend fun toggleFollow(user: UserResult): Result<Boolean>
    suspend fun profile(userId: String? = null): Result<ProfileSummary>
    suspend fun userPosts(username: String): Result<List<FeedPost>>
    suspend fun updateProfile(username: String, privacy: Boolean): Result<Unit>
    suspend fun updateAvatar(uri: Uri, username: String): Result<String>
    suspend fun social(postId: Int): Result<PostSocial>
    suspend fun toggleLike(postId: Int, currentlyLiked: Boolean): Result<Unit>
    suspend fun addComment(postId: Int, content: String, preComment: Int? = null): Result<Unit>
    suspend fun deleteComment(id: Int): Result<Unit>
}
