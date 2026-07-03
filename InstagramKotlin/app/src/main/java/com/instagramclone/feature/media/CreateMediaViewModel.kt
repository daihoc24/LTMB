package com.instagramclone.feature.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramclone.feature.media.data.MediaFileStore
import com.instagramclone.feature.media.data.MediaPickerMapper
import com.instagramclone.feature.media.model.LocalMedia
import com.instagramclone.feature.media.model.MediaResult
import com.instagramclone.feature.content.ContentRefreshBus
import com.instagramclone.feature.content.ContentRepository
import com.instagramclone.feature.content.ContentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateMediaViewModel @Inject constructor(
    private val mapper: MediaPickerMapper,
    private val files: MediaFileStore,
    private val contentRepository: ContentRepository,
    private val refreshBus: ContentRefreshBus,
) : ViewModel() {
    private val _selected = MutableStateFlow<List<LocalMedia>>(emptyList())
    val selected = _selected.asStateFlow()
    private val _caption = MutableStateFlow("")
    val caption = _caption.asStateFlow()
    private val _submitting = MutableStateFlow(false)
    val submitting = _submitting.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun updateCaption(value: String) { if (value.length <= 1000) _caption.value = value }
    fun clearMessage() { _message.value = null }

    fun submit() {
        if (_submitting.value) return
        if (_caption.value.isBlank() && _selected.value.isEmpty()) {
            _message.value = "Hãy nhập nội dung hoặc chọn ảnh"
            return
        }
        viewModelScope.launch {
            _submitting.value = true
            _message.value = null
            when (val result = contentRepository.createPost(_caption.value, _selected.value)) {
                is ContentResult.Success -> {
                    clear()
                    _caption.value = ""
                    _message.value = "Đăng bài thành công"
                    refreshBus.refresh()
                }
                is ContentResult.Failure -> _message.value = result.message
            }
            _submitting.value = false
        }
    }

    fun accept(result: MediaResult) {
        if (result is MediaResult.Selected) replace(result.items)
    }

    fun acceptPicker(uris: List<Uri>) { replace(mapper.map(uris)) }

    fun clear() { replace(emptyList()) }

    private fun replace(items: List<LocalMedia>) {
        _selected.value.filter { old -> items.none { it.uri == old.uri } }.forEach(files::delete)
        _selected.value = items
    }

    override fun onCleared() { _selected.value.forEach(files::delete); super.onCleared() }
}
