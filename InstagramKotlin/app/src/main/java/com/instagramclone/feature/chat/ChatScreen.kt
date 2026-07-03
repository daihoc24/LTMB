package com.instagramclone.feature.chat

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

private const val IMAGE_PREFIX = "__IMAGE__:"

@Composable
fun ChatScreen(onClose: () -> Unit, vm: ChatViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    BackHandler { if (state.selected != null) vm.back() else onClose() }
    if (state.selected == null) ContactList(state, vm, onClose) else Conversation(state, vm)
}

@Composable
private fun ContactList(state: ChatUiState, vm: ChatViewModel, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Đóng tin nhắn") }
            Text("Tin nhắn", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(if (state.connected) "Online" else "Offline", color = if (state.connected) Color(0xFF168A49) else Color(0xFF737373))
        }
        HorizontalDivider(color = Color(0xFFEAEAEA))
        if (!state.connected) {
            TextButton(vm::reconnect, Modifier.padding(horizontal = 12.dp)) { Text("Kết nối lại") }
        }
        state.error?.let { Text(it, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error) }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!state.loading && state.contacts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chưa có người để nhắn tin") }
        } else {
            LazyColumn {
                items(state.contacts, key = { it.id.orEmpty() }) { user ->
                    ListItem(
                        headlineContent = { Text(user.username ?: "Người dùng", style = MaterialTheme.typography.titleSmall) },
                        supportingContent = { Text("Nhấn để bắt đầu trò chuyện", color = Color(0xFF737373)) },
                        leadingContent = {
                            AsyncImage(
                                user.avatar,
                                "Avatar ${user.username}",
                                Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                                contentScale = ContentScale.Crop,
                            )
                        },
                        modifier = Modifier.fillMaxWidth().clickable { vm.open(user) },
                    )
                    HorizontalDivider(Modifier.padding(start = 80.dp), color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

@Composable
private fun Conversation(state: ChatUiState, vm: ChatViewModel) {
    val listState = rememberLazyListState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.sendImage(uri)
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(vm::back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
            AsyncImage(
                state.selected?.avatar,
                "Avatar",
                Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(state.selected?.username ?: "Chat", style = MaterialTheme.typography.titleMedium)
                Text(if (state.connected) "Đang hoạt động" else "Mất kết nối", style = MaterialTheme.typography.bodySmall, color = Color(0xFF737373))
            }
        }
        HorizontalDivider(color = Color(0xFFEAEAEA))
        state.error?.let { Text(it, Modifier.padding(8.dp), color = MaterialTheme.colorScheme.error) }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id ?: it.hashCode() }) { message ->
                val mine = message.userIdSend == state.me
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    if (message.content.startsWith(IMAGE_PREFIX)) {
                        AsyncImage(
                            model = message.content.removePrefix(IMAGE_PREFIX),
                            contentDescription = "Ảnh trong tin nhắn",
                            modifier = Modifier.widthIn(max = 240.dp).heightIn(min = 120.dp, max = 320.dp)
                                .clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F0F0)),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Surface(
                            color = if (mine) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text(
                                message.content,
                                Modifier.padding(horizontal = 14.dp, vertical = 9.dp).widthIn(max = 260.dp),
                                color = if (mine) MaterialTheme.colorScheme.onPrimary else Color(0xFF17181C),
                            )
                        }
                    }
                }
            }
        }
        if (state.sendingImage) LinearProgressIndicator(Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { imagePicker.launch("image/*") },
                enabled = state.connected && !state.sendingImage,
            ) { Icon(Icons.Default.Image, "Gửi ảnh", tint = MaterialTheme.colorScheme.primary) }
            OutlinedTextField(
                state.input,
                vm::input,
                placeholder = { Text("Nhắn tin...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(vm::send, enabled = state.input.isNotBlank() && state.connected) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Gửi")
                    }
                },
            )
        }
    }
}
