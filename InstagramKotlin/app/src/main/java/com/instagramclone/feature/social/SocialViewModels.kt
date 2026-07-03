package com.instagramclone.feature.social

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(val query: String = "", val users: List<UserResult> = emptyList(), val posts: List<PostResult> = emptyList(), val userMode: Boolean = true, val loading: Boolean = false, val error: String? = null)

@HiltViewModel class SearchViewModel @Inject constructor(private val repo: SocialRepository) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState()); val state = _state.asStateFlow()
    fun query(value: String) { _state.value = _state.value.copy(query = value.take(100)) }
    fun mode(users: Boolean) { _state.value = _state.value.copy(userMode = users, users = emptyList(), posts = emptyList()) }
    fun search() { if (_state.value.query.isBlank()) return; viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        if (_state.value.userMode) repo.searchUsers(_state.value.query).fold(
            { _state.value = _state.value.copy(users = it, loading = false) },
            { _state.value = _state.value.copy(error = it.message, loading = false) },
        ) else repo.searchPosts(_state.value.query).fold(
            { _state.value = _state.value.copy(posts = it, loading = false) },
            { _state.value = _state.value.copy(error = it.message, loading = false) },
        )
    } }
    fun follow(user: UserResult) { viewModelScope.launch { repo.toggleFollow(user).onSuccess { following ->
        _state.value = _state.value.copy(users = _state.value.users.map { if (it.id == user.id) it.copy(following = following) else it })
    }.onFailure { _state.value = _state.value.copy(error = it.message) } } }
}

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: ProfileSummary? = null,
    val posts: List<com.instagramclone.feature.content.FeedPost> = emptyList(),
    val postCount: Int = 0,
    val isMe: Boolean = true,
    val editing: Boolean = false,
    val username: String = "",
    val privacy: Boolean = false,
    val avatarUri: String? = null,
    val saving: Boolean = false,
    val message: String? = null,
)

@HiltViewModel class ProfileViewModel @Inject constructor(
    private val repo: SocialRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState()); val state = _state.asStateFlow()
    private val requestedUserId: String? = savedStateHandle.get<String>("userId")?.takeIf { it.isNotBlank() }
    init { load() }
    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, message = null)
        repo.profile(requestedUserId).fold(
            { profile ->
                val isMe = profile.id == repo.currentUserId()
                val canShowPosts = isMe || !profile.privacy || profile.followedByMe
                val allPosts = repo.userPosts(profile.username).getOrDefault(emptyList())
                val posts = if (canShowPosts) allPosts else emptyList()
                _state.value = ProfileUiState(false, profile, posts, allPosts.size, isMe, username = profile.username, privacy = profile.privacy)
            },
            { _state.value = _state.value.copy(loading = false, message = it.message) },
        )
    }
    fun edit(value: Boolean) { _state.value = _state.value.copy(editing = value) }
    fun username(value: String) { _state.value = _state.value.copy(username = value.take(50)) }
    fun privacy(value: Boolean) { _state.value = _state.value.copy(privacy = value) }
    fun avatar(uri: Uri) { _state.value = _state.value.copy(avatarUri = uri.toString(), message = null) }
    fun save() { if (_state.value.username.length < 3) return; viewModelScope.launch { repo.updateProfile(_state.value.username, _state.value.privacy).fold(
        { _state.value = _state.value.copy(editing = false, profile = _state.value.profile?.copy(username = _state.value.username, privacy = _state.value.privacy), message = "Đã cập nhật") },
        { _state.value = _state.value.copy(message = it.message) },
    ) } }
    fun saveAll() {
        val current = _state.value
        val profile = current.profile ?: return
        if (current.username.length < 3 || current.saving) return
        viewModelScope.launch {
            _state.value = current.copy(saving = true, message = null)
            val avatarUrl = current.avatarUri?.let { value ->
                repo.updateAvatar(Uri.parse(value), profile.username).fold(
                    { it },
                    {
                        _state.value = _state.value.copy(saving = false, message = it.message)
                        return@launch
                    },
                )
            }
            repo.updateProfile(current.username, current.privacy).fold(
                {
                    _state.value = _state.value.copy(
                        editing = false,
                        saving = false,
                        avatarUri = null,
                        profile = profile.copy(
                            username = current.username,
                            privacy = current.privacy,
                            avatar = avatarUrl ?: profile.avatar,
                        ),
                        message = "Đã cập nhật trang cá nhân",
                    )
                },
                { _state.value = _state.value.copy(saving = false, message = it.message) },
            )
        }
    }
    fun toggleFollow() {
        val profile = _state.value.profile ?: return
        if (_state.value.isMe) return
        viewModelScope.launch {
            repo.toggleFollow(UserResult(profile.id, profile.username, profile.avatar, profile.followedByMe)).fold(
                { followed ->
                    val delta = if (followed) 1 else -1
                    _state.value = _state.value.copy(
                        profile = profile.copy(followedByMe = followed, followers = (profile.followers + delta).coerceAtLeast(0)),
                    )
                    if (!profile.privacy || followed) load()
                    else _state.value = _state.value.copy(posts = emptyList())
                },
                { _state.value = _state.value.copy(message = it.message) },
            )
        }
    }
}
