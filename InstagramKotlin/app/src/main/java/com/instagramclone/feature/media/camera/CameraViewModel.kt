package com.instagramclone.feature.media.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import com.instagramclone.feature.media.data.MediaFileStore
import com.instagramclone.feature.media.model.LocalMedia
import com.instagramclone.feature.media.model.MediaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class CameraUiState(
    val permissionGranted: Boolean = false,
    val capturing: Boolean = false,
    val media: LocalMedia? = null,
    val frontLens: Boolean = false,
    val frontAvailable: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val controller: CameraController,
    private val files: MediaFileStore,
) : ViewModel() {
    private val _state = MutableStateFlow(CameraUiState(frontAvailable = controller.hasFrontCamera()))
    val state: StateFlow<CameraUiState> = _state.asStateFlow()
    private val results = Channel<MediaResult>(Channel.BUFFERED)
    val result = results.receiveAsFlow()
    private var pendingFile: File? = null

    fun permission(granted: Boolean) { _state.value = _state.value.copy(permissionGranted = granted, error = null) }
    fun cameraError(message: String) { _state.value = _state.value.copy(capturing = false, error = message) }
    fun dismissError() { _state.value = _state.value.copy(error = null) }
    fun switchLens() { if (_state.value.frontAvailable) _state.value = _state.value.copy(frontLens = !_state.value.frontLens) }
    fun bind(owner: LifecycleOwner, previewView: PreviewView) =
        controller.bind(owner, previewView, _state.value.frontLens, ::cameraError)
    fun unbind() = controller.unbind()

    fun capture() {
        if (_state.value.capturing || _state.value.media != null) return
        val file = runCatching { files.createImageFile() }.getOrElse { return cameraError("Không thể tạo ảnh tạm") }
        pendingFile = file
        _state.value = _state.value.copy(capturing = true, error = null)
        controller.capture(file, onSuccess = {
            pendingFile = null
            _state.value = _state.value.copy(capturing = false, media = files.toMedia(file))
        }, onError = { message -> files.delete(file); pendingFile = null; cameraError(message) })
    }

    fun retake() { files.delete(_state.value.media); _state.value = _state.value.copy(media = null, error = null) }
    fun use(requestKey: String) { _state.value.media?.let { results.trySend(MediaResult.Selected(requestKey, listOf(it))) } }
    fun cancel(requestKey: String) { files.delete(_state.value.media); results.trySend(MediaResult.Cancelled(requestKey)) }

    override fun onCleared() { files.delete(pendingFile); controller.unbind(); super.onCleared() }
}
