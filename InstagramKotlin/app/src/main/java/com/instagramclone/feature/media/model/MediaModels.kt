package com.instagramclone.feature.media.model

import android.net.Uri

enum class MediaSource { CAMERA, PHOTO_PICKER }

data class LocalMedia(
    val uri: Uri,
    val source: MediaSource,
    val mimeType: String = "image/jpeg",
    val displayName: String? = null,
    val sizeBytes: Long? = null,
    val temporary: Boolean = source == MediaSource.CAMERA,
)

data class MediaRequest(val requestKey: String, val maxItems: Int = 1) {
    init { require(maxItems in 1..10) }
}

sealed interface MediaResult {
    val requestKey: String
    data class Selected(override val requestKey: String, val items: List<LocalMedia>) : MediaResult
    data class Cancelled(override val requestKey: String) : MediaResult
}
