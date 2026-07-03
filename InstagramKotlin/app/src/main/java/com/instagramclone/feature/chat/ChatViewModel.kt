package com.instagramclone.feature.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramclone.data.remote.chat.ChatMessageDto
import com.instagramclone.data.remote.chat.ChatUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val contacts: List<ChatUserDto> = emptyList(), val selected: ChatUserDto? = null,
    val messages: List<ChatMessageDto> = emptyList(), val input: String = "",
    val loading: Boolean = true, val connected: Boolean = false, val error: String? = null,
    val me: String = "",
    val sendingImage: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(private val repo: ChatRepository) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState(me = repo.currentUserId())); val state = _state.asStateFlow()
    init {
        loadContacts(); repo.connect()
        viewModelScope.launch { repo.connection().collect { _state.value = _state.value.copy(connected = it) } }
        viewModelScope.launch { repo.signals().collect { if (_state.value.selected != null) loadHistory() else loadContacts() } }
    }
    fun loadContacts() = viewModelScope.launch { _state.value = _state.value.copy(loading = true); repo.contacts().fold(
        { _state.value = _state.value.copy(contacts = it, loading = false, error = null) },
        { _state.value = _state.value.copy(loading = false, error = it.message) },
    ) }
    fun open(user: ChatUserDto) { _state.value = _state.value.copy(selected = user, messages = emptyList()); loadHistory() }
    fun back() { _state.value = _state.value.copy(selected = null, messages = emptyList(), input = "") }
    fun input(value: String) { _state.value = _state.value.copy(input = value.take(1000)) }
    fun reconnect() { repo.connect() }
    fun send() {
        val other = _state.value.selected?.id ?: return; val text = _state.value.input.trim(); if (text.isEmpty()) return
        if (!repo.send(other, text)) { _state.value = _state.value.copy(error = "Chat chưa kết nối"); return }
        _state.value = _state.value.copy(input = "")
        viewModelScope.launch { delay(300); loadHistory() }
    }
    fun sendImage(uri: Uri) {
        val other = _state.value.selected?.id ?: return
        if (_state.value.sendingImage) return
        viewModelScope.launch {
            _state.value = _state.value.copy(sendingImage = true, error = null)
            repo.sendImage(other, uri).fold(
                {
                    _state.value = _state.value.copy(sendingImage = false)
                    delay(300)
                    loadHistory()
                },
                { _state.value = _state.value.copy(sendingImage = false, error = it.message) },
            )
        }
    }
    private fun loadHistory() { val id = _state.value.selected?.id ?: return; viewModelScope.launch { repo.history(id).fold(
        { _state.value = _state.value.copy(messages = it, loading = false, error = null) },
        { _state.value = _state.value.copy(error = it.message, loading = false) },
    ) } }
    override fun onCleared() { repo.close(); super.onCleared() }
}
