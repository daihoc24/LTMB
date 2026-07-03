package com.instagramclone.feature.media.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.instagramclone.feature.media.model.LocalMedia
import com.instagramclone.feature.media.model.MediaSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaFileStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val cameraDir get() = File(context.cacheDir, "camera").apply { mkdirs() }

    fun createImageFile(): File = File.createTempFile("capture_", ".jpg", cameraDir)

    fun toMedia(file: File): LocalMedia = LocalMedia(
        uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file),
        source = MediaSource.CAMERA,
        displayName = file.name,
        sizeBytes = file.length(),
    )

    fun delete(media: LocalMedia?) {
        if (media?.temporary != true) return
        runCatching { File(media.uri.path.orEmpty()).delete() }
        val name = media.displayName ?: return
        runCatching { File(cameraDir, name).delete() }
    }

    fun delete(file: File?) { file?.let { runCatching { it.delete() } } }
}
