package com.instagramclone.feature.media.camera

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.instagramclone.feature.media.model.MediaResult

@Composable
fun CameraScreen(requestKey: String, onResult: (MediaResult) -> Unit, viewModel: CameraViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), viewModel::permission)
    LaunchedEffect(Unit) {
        viewModel.permission(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        viewModel.result.collect(onResult)
    }
    BackHandler { viewModel.cancel(requestKey) }

    when {
        !state.permissionGranted -> PermissionContent(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }, onCancel = { viewModel.cancel(requestKey) })
        state.media != null -> ReviewContent(state.media!!.uri.toString(), onRetake = viewModel::retake, onUse = { viewModel.use(requestKey) })
        else -> CameraPreview(state, viewModel)
    }
}

@Composable
private fun PermissionContent(onRequest: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Cần quyền camera để chụp ảnh")
        Button(onClick = onRequest, modifier = Modifier.padding(top = 16.dp).testTag("camera-permission-button")) { Text("Cho phép") }
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
            },
            modifier = Modifier.padding(top = 8.dp),
        ) { Text("Mở cài đặt") }
        Button(onClick = onCancel, modifier = Modifier.padding(top = 8.dp)) { Text("Quay lại") }
    }
}

@Composable
private fun CameraPreview(state: CameraUiState, viewModel: CameraViewModel) {
    val owner = LocalLifecycleOwner.current
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context -> PreviewView(context).also { viewModel.bind(owner, it) } },
            update = { viewModel.bind(owner, it) },
            modifier = Modifier.fillMaxSize(),
        )
        Row(Modifier.align(Alignment.BottomCenter).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            if (state.frontAvailable) IconButton(onClick = viewModel::switchLens) { Icon(Icons.Default.Cameraswitch, "Đổi camera") }
            Button(onClick = viewModel::capture, enabled = !state.capturing, modifier = Modifier.testTag("camera-capture-button")) { Text("Chụp") }
            if (state.capturing) CircularProgressIndicator()
        }
        state.error?.let { Text(it, Modifier.align(Alignment.TopCenter).padding(24.dp)) }
    }
    DisposableEffect(Unit) { onDispose { viewModel.unbind() } }
}

@Composable
private fun ReviewContent(uri: String, onRetake: () -> Unit, onUse: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AsyncImage(model = uri, contentDescription = "Ảnh vừa chụp", modifier = Modifier.weight(1f).fillMaxSize())
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetake, modifier = Modifier.weight(1f)) { Text("Chụp lại") }
            Button(onClick = onUse, modifier = Modifier.weight(1f)) { Text("Dùng ảnh") }
        }
    }
}
