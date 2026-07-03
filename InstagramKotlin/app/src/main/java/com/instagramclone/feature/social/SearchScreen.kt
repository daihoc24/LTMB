package com.instagramclone.feature.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable fun SearchScreen(
    onPostClick: (Int) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    vm: SearchViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(s.query, vm::query, label = { Text("Tìm kiếm") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(s.userMode, { vm.mode(true) }, { Text("Người dùng") })
            FilterChip(!s.userMode, { vm.mode(false) }, { Text("Bài viết") })
            Button(vm::search, enabled = !s.loading && s.query.isNotBlank()) { Text("Tìm") }
        }
        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (s.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (s.userMode) items(s.users, key = { it.id }) { user ->
                Card(Modifier.fillMaxWidth().clickable { onUserClick(user.id) }) { Row(Modifier.padding(12.dp)) {
                    AsyncImage(user.avatar, "Ảnh đại diện ${user.username}", Modifier.size(44.dp)); Text(user.username, Modifier.padding(12.dp).weight(1f))
                    Button({ vm.follow(user) }) { Text(if (user.following) "Bỏ theo dõi" else "Theo dõi") }
                } }
            } else items(s.posts, key = { it.id }) { post ->
                Card(Modifier.fillMaxWidth().clickable { onPostClick(post.id) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(post.username, style = MaterialTheme.typography.labelLarge)
                        Text(post.caption, style = MaterialTheme.typography.bodyMedium)
                        Text("Xem bài viết", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
