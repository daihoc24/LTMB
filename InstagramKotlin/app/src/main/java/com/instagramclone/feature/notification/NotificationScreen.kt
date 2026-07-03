package com.instagramclone.feature.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable fun NotificationScreen(vm: NotificationViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Thông báo", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(vm::refresh) { Icon(Icons.Default.Refresh, "Làm mới thông báo") }
        }
        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            s.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(s.error!!); Button(vm::refresh) { Text("Thử lại") } } }
            s.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Chưa có thông báo") }
            else -> LazyColumn { itemsIndexed(s.items, key = { index, item -> "$index-${item.message}" }) { _, item ->
                ListItem(headlineContent = { Text(item.message) }, leadingContent = { AsyncImage(item.avatar, "Ảnh thông báo", Modifier.size(48.dp)) })
                HorizontalDivider()
            } }
        }
    }
}
