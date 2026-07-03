package com.instagramclone.feature.media

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.instagramclone.feature.media.camera.CameraScreen

private const val CREATE_REQUEST = "create-post-media"

@Composable
fun CreateMediaScreen(viewModel: CreateMediaViewModel = hiltViewModel()) {
    var cameraOpen by remember { mutableStateOf(false) }
    val selected by viewModel.selected.collectAsState()
    val caption by viewModel.caption.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val message by viewModel.message.collectAsState()
    val canSubmit = !submitting && (caption.isNotBlank() || selected.isNotEmpty())
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) {
        viewModel.acceptPicker(it)
    }
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearMessage()
        }
    }

    if (cameraOpen) {
        CameraScreen(CREATE_REQUEST, onResult = { result -> viewModel.accept(result); cameraOpen = false })
        return
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = viewModel::clear) { Text("Hủy", color = Color(0xFF111111)) }
            Text(
                "Bài viết mới",
                Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(
                onClick = viewModel::submit,
                enabled = canSubmit,
                modifier = Modifier.testTag("create-post-submit-button"),
            ) {
                if (submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Chia sẻ", fontWeight = FontWeight.SemiBold)
            }
        }
        HorizontalDivider(color = Color(0xFFE8E8E8))

        selected.firstOrNull()?.let { media ->
            Box(Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = media.uri,
                    contentDescription = "Ảnh đã chọn",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                )
                if (selected.size > 1) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Black.copy(alpha = 0.68f),
                    ) { Text("1/${selected.size}", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color.White) }
                }
            }
        } ?: Box(
            Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoLibrary, null, Modifier.size(42.dp), tint = Color(0xFF777777))
                Text("Chọn ảnh để tạo bài viết", Modifier.padding(top = 10.dp), color = Color(0xFF737373))
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = { cameraOpen = true },
                modifier = Modifier.weight(1f).testTag("open-camera-button"),
                shape = RoundedCornerShape(10.dp),
            ) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(6.dp)); Text("Camera") }
            Button(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f).testTag("open-photo-picker-button"),
                shape = RoundedCornerShape(10.dp),
            ) { Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Thư viện") }
        }

        HorizontalDivider(color = Color(0xFFE8E8E8))
        TextField(
            value = caption,
            onValueChange = viewModel::updateCaption,
            placeholder = { Text("Viết chú thích…", color = Color(0xFF737373)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selected.isNotEmpty()) {
                Text("Đã chọn ${selected.size} ảnh", Modifier.weight(1f), color = Color(0xFF737373), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = viewModel::clear) { Text("Bỏ ảnh", color = Color(0xFFED4956)) }
            } else Spacer(Modifier.weight(1f))
            Text("${caption.length}/1000", color = Color(0xFF737373), style = MaterialTheme.typography.bodySmall)
        }
        message?.let {
            Snackbar(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                containerColor = if (it.contains("thành công", true)) Color(0xFF168A49) else MaterialTheme.colorScheme.error,
                contentColor = Color.White,
                dismissAction = {
                    TextButton(onClick = viewModel::clearMessage) { Text("Đóng", color = Color.White) }
                },
            ) { Text(it) }
        }
    }
}
