package com.instagramclone.feature.media.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.instagramclone.feature.media.model.LocalMedia
import com.instagramclone.feature.media.model.MediaSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaPickerMapper @Inject constructor(@ApplicationContext private val context: Context) {
    fun map(uris: List<Uri>, maxItems: Int = 10): List<LocalMedia> = uris.distinct().take(maxItems).mapNotNull(::mapOne)

    private fun mapOne(uri: Uri): LocalMedia? = runCatching {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: return null
        if (!mime.startsWith("image/")) return null
        var name: String? = null
        var size: Long? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use {
            if (it.moveToFirst()) {
                name = it.getString(0)
                size = if (it.isNull(1)) null else it.getLong(1)
            }
        }
        resolver.openInputStream(uri)?.use { }
        LocalMedia(uri, MediaSource.PHOTO_PICKER, mime, name, size, temporary = false)
    }.getOrNull()
}
