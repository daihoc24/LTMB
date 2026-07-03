package com.instagramclone.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramclone.data.remote.notification.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(val loading: Boolean = true, val items: List<NotificationDto> = emptyList(), val error: String? = null)

@HiltViewModel
class NotificationViewModel @Inject constructor(private val repository: NotificationRepository) : ViewModel() {
    private val _state = MutableStateFlow(NotificationUiState()); val state = _state.asStateFlow()
    fun refresh() { viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        repository.load().fold(
            { _state.value = NotificationUiState(false, it) },
            { _state.value = _state.value.copy(loading = false, error = it.message) },
        )
    } }
}
