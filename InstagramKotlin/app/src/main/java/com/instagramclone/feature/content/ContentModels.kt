package com.instagramclone.feature.content

import com.instagramclone.feature.media.model.LocalMedia

data class FeedPost(
    val id: Int,
    val userId: String,
    val caption: String,
    val username: String,
    val avatarUrl: String?,
    val createdAt: String?,
    val mediaUrls: List<String>,
)

sealed interface ContentResult<out T> {
    data class Success<T>(val value: T) : ContentResult<T>
    data class Failure(val message: String) : ContentResult<Nothing>
}

interface ContentRepository {
    suspend fun loadFeed(): ContentResult<List<FeedPost>>
    suspend fun loadMedia(posts: List<FeedPost>): ContentResult<Map<Int, List<String>>>
    suspend fun loadMedia(post: FeedPost): ContentResult<List<String>>
    suspend fun createPost(caption: String, media: List<LocalMedia>): ContentResult<Unit>
    suspend fun updateCaption(postId: Int, caption: String): ContentResult<Unit>
    suspend fun hidePost(postId: Int): ContentResult<Unit>
}
