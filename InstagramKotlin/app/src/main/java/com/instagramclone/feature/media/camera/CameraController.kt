package com.instagramclone.feature.media.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraController @Inject constructor(@ApplicationContext private val context: Context) {
    private var provider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var boundPreviewView: PreviewView? = null
    private var boundFront: Boolean? = null
    private var binding = false

    fun bind(owner: LifecycleOwner, previewView: PreviewView, front: Boolean, onError: (String) -> Unit) {
        if ((binding || imageCapture != null) && boundPreviewView === previewView && boundFront == front) return
        binding = true
        boundPreviewView = previewView
        boundFront = front
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            runCatching {
                val cameraProvider = future.get()
                val selector = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val capture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(owner, selector, preview, capture)
                provider = cameraProvider
                imageCapture = capture
            }.onFailure { onError(it.message ?: "Không thể mở camera") }
            binding = false
        }, ContextCompat.getMainExecutor(context))
    }

    fun hasFrontCamera(): Boolean = context.packageManager.hasSystemFeature("android.hardware.camera.front")

    fun capture(file: File, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val capture = imageCapture ?: return onError("Camera chưa sẵn sàng")
        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) = onSuccess()
                override fun onError(exception: ImageCaptureException) = onError(exception.message ?: "Chụp ảnh thất bại")
            },
        )
    }

    fun unbind() {
        provider?.unbindAll()
        imageCapture = null
        boundPreviewView = null
        boundFront = null
        binding = false
    }
}
