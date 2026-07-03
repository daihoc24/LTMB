package com.instagramclone.feature.social

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
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

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onPostClick: (Int) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onMessage: (() -> Unit)? = null,
    vm: ProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.avatar(uri)
    }

    if (state.loading && state.profile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val profile = state.profile
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
            Text(profile?.username.orEmpty(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (state.isMe) Box {
                IconButton({ menuOpen = true }) { Icon(Icons.Default.Menu, "Menu trang cá nhân") }
                DropdownMenu(menuOpen, { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuOpen = false; onLogout() },
                    )
                }
            }
        }
        HorizontalDivider(color = Color(0xFFEAEAEA))

        if (state.editing && state.isMe) {
            Column(Modifier.padding(16.dp)) {
                AsyncImage(
                    model = state.avatarUri?.let(Uri::parse) ?: profile?.avatar,
                    contentDescription = "Ảnh đại diện mới",
                    modifier = Modifier.size(96.dp).align(Alignment.CenterHorizontally)
                        .clip(CircleShape).background(Color(0xFFF0F0F0)),
                    contentScale = ContentScale.Crop,
                )
                TextButton(
                    onClick = { avatarPicker.launch("image/*") },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) { Text("Thay đổi ảnh đại diện") }
                OutlinedTextField(state.username, vm::username, label = { Text("Tên người dùng") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tài khoản riêng tư", Modifier.weight(1f)); Switch(state.privacy, vm::privacy)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(vm::saveAll, enabled = !state.saving) {
                        if (state.saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Lưu")
                    }
                    TextButton({ vm.edit(false) }) { Text("Hủy") }
                }
            }
        } else {
            ProfileHeader(state, vm::toggleFollow, { vm.edit(true) }, onMessage)
            if (profile?.privacy == true && !state.isMe && !profile.followedByMe) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Đây là tài khoản riêng tư")
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().height(44.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) { Icon(Icons.Default.GridOn, "Lưới bài viết") }
                HorizontalDivider(color = Color(0xFFEAEAEA))
                if (state.posts.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("Chưa có bài viết") }
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.posts, key = { it.id }) { post ->
                            AsyncImage(
                                model = post.mediaUrls.firstOrNull(),
                                contentDescription = "Bài viết ${post.id}",
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(1.dp)
                                    .background(Color(0xFFF0F0F0)).clickable { onPostClick(post.id) },
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
        }
        state.message?.let { Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileUiState,
    onFollow: () -> Unit,
    onEdit: () -> Unit,
    onMessage: (() -> Unit)?,
) {
    val profile = state.profile ?: return
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                profile.avatar,
                "Ảnh đại diện ${profile.username}",
                Modifier.size(86.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(20.dp))
            Stat(state.postCount, "Bài viết", Modifier.weight(1f))
            Stat(profile.followers, "Follower", Modifier.weight(1f))
            Stat(profile.following, "Following", Modifier.weight(1f))
        }
        Text(profile.username, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
        if (state.isMe && !profile.email.isNullOrBlank()) Text(profile.email, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        if (state.isMe) {
            OutlinedButton(onEdit, Modifier.fillMaxWidth()) { Text("Chỉnh sửa trang cá nhân") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onFollow, Modifier.weight(1f)) { Text(if (profile.followedByMe) "Đang theo dõi" else "Theo dõi") }
                OutlinedButton({ onMessage?.invoke() }, Modifier.weight(1f), enabled = onMessage != null) { Text("Nhắn tin") }
            }
        }
    }
}

@Composable
private fun Stat(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
