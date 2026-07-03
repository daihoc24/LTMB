package com.instagramclone.feature.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.instagramclone.feature.social.PostSocial
import com.instagramclone.feature.social.SocialRepository
import com.instagramclone.data.remote.social.CommentDto

data class ReplyTarget(val parentId: Int, val username: String)

data class FeedUiState(
    val loading: Boolean = true,
    val posts: List<FeedPost> = emptyList(),
    val mediaLoading: Boolean = false,
    val social: Map<Int, PostSocial> = emptyMap(),
    val commentInputs: Map<Int, String> = emptyMap(),
    val replyTargets: Map<Int, ReplyTarget> = emptyMap(),
    val currentUserId: String = "",
    val editingPostId: Int? = null,
    val editCaption: String = "",
    val error: String? = null,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: ContentRepository,
    private val socialRepository: SocialRepository,
    refreshBus: ContentRefreshBus,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedUiState(currentUserId = runCatching { socialRepository.currentUserId() }.getOrDefault("")))
    val state = _state.asStateFlow()
    private var mediaJob: Job? = null
    private val socialRequests = Semaphore(permits = 2)
    private val mediaRequests = Semaphore(permits = 3)

    init {
        refresh()
        viewModelScope.launch { refreshBus.events.collect { refresh() } }
    }

    fun refresh() {
        if (_state.value.loading && _state.value.posts.isNotEmpty()) return
        mediaJob?.cancel()
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val result = repository.loadFeed()) {
                is ContentResult.Success -> {
                    _state.value = _state.value.copy(loading = false, posts = result.value)
                    result.value.forEach { loadSocial(it.id) }
                    loadMediaInBackground(result.value)
                }
                is ContentResult.Failure -> _state.value = _state.value.copy(loading = false, error = result.message)
            }
        }
    }

    private fun loadMediaInBackground(posts: List<FeedPost>) {
        _state.value = _state.value.copy(mediaLoading = posts.isNotEmpty())
        mediaJob = viewModelScope.launch {
            posts.map { post ->
                launch {
                    mediaRequests.withPermit {
                        when (val result = repository.loadMedia(post)) {
                            is ContentResult.Success -> _state.value = _state.value.copy(
                                posts = _state.value.posts.map { current ->
                                    if (current.id == post.id) current.copy(mediaUrls = result.value) else current
                                },
                            )
                            is ContentResult.Failure -> Unit
                        }
                    }
                }
            }.forEach { it.join() }
            _state.value = _state.value.copy(mediaLoading = false)
        }
    }

    fun commentInput(postId: Int, value: String) {
        _state.value = _state.value.copy(commentInputs = _state.value.commentInputs + (postId to value.take(500)))
    }

    fun startReply(postId: Int, comment: CommentDto) {
        val parentId = if (comment.preComment > 0) comment.preComment else comment.id
        _state.value = _state.value.copy(
            replyTargets = _state.value.replyTargets + (postId to ReplyTarget(parentId, comment.username ?: "User")),
        )
    }

    fun cancelReply(postId: Int) {
        _state.value = _state.value.copy(replyTargets = _state.value.replyTargets - postId)
    }

    fun toggleLike(postId: Int) {
        val current = _state.value.social[postId] ?: return
        val optimistic = current.copy(
            liked = !current.liked,
            likes = (current.likes + if (current.liked) -1 else 1).coerceAtLeast(0),
        )
        _state.value = _state.value.copy(social = _state.value.social + (postId to optimistic))
        viewModelScope.launch {
            socialRepository.toggleLike(postId, current.liked).onFailure {
                _state.value = _state.value.copy(social = _state.value.social + (postId to current))
            }
        }
    }

    fun addComment(postId: Int) {
        val text = _state.value.commentInputs[postId].orEmpty()
        val reply = _state.value.replyTargets[postId]
        if (text.isBlank()) return
        viewModelScope.launch { socialRepository.addComment(postId, text, reply?.parentId).fold(
            { _state.value = _state.value.copy(commentInputs = _state.value.commentInputs - postId, replyTargets = _state.value.replyTargets - postId); loadSocial(postId) },
            { setSocialError(postId, it.message) },
        ) }
    }

    fun deleteComment(postId: Int, commentId: Int) {
        viewModelScope.launch { socialRepository.deleteComment(commentId).fold(
            { loadSocial(postId) }, { setSocialError(postId, it.message) },
        ) }
    }

    fun startEdit(post: FeedPost) { _state.value = _state.value.copy(editingPostId = post.id, editCaption = post.caption) }
    fun editCaption(value: String) { _state.value = _state.value.copy(editCaption = value.take(1000)) }
    fun cancelEdit() { _state.value = _state.value.copy(editingPostId = null, editCaption = "") }
    fun saveEdit(postId: Int) { viewModelScope.launch { when (val result = repository.updateCaption(postId, _state.value.editCaption)) {
        is ContentResult.Success -> { cancelEdit(); refresh() }
        is ContentResult.Failure -> _state.value = _state.value.copy(error = result.message)
    } } }
    fun hidePost(postId: Int) { viewModelScope.launch { when (val result = repository.hidePost(postId)) {
        is ContentResult.Success -> refresh()
        is ContentResult.Failure -> _state.value = _state.value.copy(error = result.message)
    } } }

    private fun loadSocial(postId: Int) { viewModelScope.launch {
        socialRequests.withPermit {
            socialRepository.social(postId).onSuccess { value ->
                _state.value = _state.value.copy(social = _state.value.social + (postId to value))
            }.onFailure {
                // Dữ liệu like/comment không được làm hỏng hoặc che phần media của bài viết.
                val old = _state.value.social[postId] ?: PostSocial()
                _state.value = _state.value.copy(social = _state.value.social + (postId to old))
            }
        }
    } }

    private fun setSocialError(postId: Int, message: String?) {
        val old = _state.value.social[postId] ?: PostSocial()
        _state.value = _state.value.copy(social = _state.value.social + (postId to old.copy(error = message ?: "Thao tác thất bại")))
    }
}
