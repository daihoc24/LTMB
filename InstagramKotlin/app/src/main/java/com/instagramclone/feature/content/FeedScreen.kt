package com.instagramclone.feature.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.instagramclone.R
import com.instagramclone.feature.social.PostSocial
import com.instagramclone.data.remote.social.CommentDto

@Composable
fun FeedScreen(
    onOpenChat: () -> Unit,
    onUserClick: (String) -> Unit = {},
    focusPostId: Int? = null,
    onBack: (() -> Unit)? = null,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
                Text("Bài viết", style = MaterialTheme.typography.titleMedium)
            } else {
                Icon(painterResource(R.drawable.ic_instagram_logo), "Instagram", Modifier.size(30.dp))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenChat) { Icon(Icons.AutoMirrored.Filled.Send, "Mở tin nhắn") }
            IconButton(onClick = viewModel::refresh) { Icon(Icons.Default.Refresh, "Làm mới") }
        }
        HorizontalDivider(color = Color(0xFFEAEAEA))
        if (state.mediaLoading) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
        when {
            state.loading && state.posts.isEmpty() -> Centered { CircularProgressIndicator() }
            state.error != null && state.posts.isEmpty() -> Centered {
                Text(state.error!!)
                IconButton(onClick = viewModel::refresh) { Icon(Icons.Default.Refresh, "Thử lại") }
            }
            state.posts.isEmpty() -> Centered { Text("Chưa có bài viết") }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(state.posts.filter { focusPostId == null || it.id == focusPostId }, key = { it.id }) { post ->
                    PostCard(
                        post, state.social[post.id], state.commentInputs[post.id].orEmpty(), state.replyTargets[post.id],
                        state.currentUserId, state.editingPostId == post.id, state.editCaption,
                        { viewModel.toggleLike(post.id) }, { viewModel.commentInput(post.id, it) },
                        { viewModel.addComment(post.id) }, { viewModel.deleteComment(post.id, it) },
                        { viewModel.startReply(post.id, it) }, { viewModel.cancelReply(post.id) },
                        { viewModel.startEdit(post) }, viewModel::editCaption,
                        { viewModel.saveEdit(post.id) }, viewModel::cancelEdit, { viewModel.hidePost(post.id) },
                        { onUserClick(post.userId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    content = content,
)

@Composable
private fun PostCard(
    post: FeedPost, social: PostSocial?, input: String, replyTarget: ReplyTarget?, currentUserId: String,
    editing: Boolean, editCaption: String, onLike: () -> Unit,
    onInput: (String) -> Unit, onComment: () -> Unit, onDeleteComment: (Int) -> Unit,
    onReply: (CommentDto) -> Unit, onCancelReply: () -> Unit,
    onStartEdit: () -> Unit, onEditCaption: (String) -> Unit, onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit, onHide: () -> Unit, onUserClick: () -> Unit,
) {
    var postMenuExpanded by rememberSaveable(post.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(Color.White)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp).clickable(onClick = onUserClick), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(post.avatarUrl, "Ảnh đại diện ${post.username}", Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Text(post.username, Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.titleSmall)
            if (post.userId == currentUserId && !editing) {
                Box {
                    IconButton(onClick = { postMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, "Tùy chọn bài viết")
                    }
                    DropdownMenu(
                        expanded = postMenuExpanded,
                        onDismissRequest = { postMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sửa bài viết") },
                            onClick = {
                                postMenuExpanded = false
                                onStartEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Ẩn bài viết", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                postMenuExpanded = false
                                onHide()
                            },
                        )
                    }
                }
            }
        }
        if (editing) {
            OutlinedTextField(editCaption, onEditCaption, Modifier.fillMaxWidth().padding(horizontal = 12.dp), label = { Text("Chú thích") })
            Row { Button(onSaveEdit) { Text("Lưu") }; TextButton(onCancelEdit) { Text("Hủy") } }
        } else if (post.caption.isNotBlank()) {
            Text(post.caption, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
        }
        if (false && post.userId == currentUserId && !editing) Row(Modifier.padding(horizontal = 4.dp)) {
            TextButton(onStartEdit) { Text("Sửa") }; TextButton(onHide) { Text("Ẩn bài") }
        }
        post.mediaUrls.firstOrNull()?.let { url ->
            AsyncImage(url, "Ảnh bài viết của ${post.username}", Modifier.fillMaxWidth().aspectRatio(1f), contentScale = ContentScale.Crop)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLike) {
                Icon(
                    if (social?.liked == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    "Thích",
                    tint = if (social?.liked == true) Color(0xFFED4956) else Color(0xFF111111),
                )
            }
            Text("${social?.likes ?: 0} lượt thích", style = MaterialTheme.typography.labelMedium)
        }
        CommentList(social, currentUserId, onDeleteComment, onReply)
        replyTarget?.let {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Đang trả lời @${it.username}", Modifier.weight(1f), color = Color(0xFF737373), style = MaterialTheme.typography.bodySmall)
                Text("Hủy", Modifier.clickable(onClick = onCancelReply).padding(6.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                input, onInput, Modifier.weight(1f), placeholder = { Text(if (replyTarget == null) "Thêm bình luận…" else "Trả lời @${replyTarget.username}…") },
                singleLine = true, shape = RoundedCornerShape(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onComment,
                enabled = input.isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            ) { Text("Đăng") }
        }
        HorizontalDivider(color = Color(0xFFE8E8E8))
    }
}

@Composable
private fun CommentList(
    social: PostSocial?,
    currentUserId: String,
    onDeleteComment: (Int) -> Unit,
    onReply: (CommentDto) -> Unit,
) {
    val comments = social?.comments.orEmpty()
    val roots = comments.filter { it.preComment <= 0 || comments.none { parent -> parent.id == it.preComment } }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shown = if (expanded) roots else roots.take(3)

    shown.forEach { root ->
        CommentRow(root, currentUserId, false, onDeleteComment, onReply)
        comments.filter { it.preComment == root.id }.forEach { reply ->
            CommentRow(reply, currentUserId, true, onDeleteComment, onReply)
        }
    }
    if (roots.size > 3) {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(horizontal = 4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                if (expanded) "Thu gọn" else "Xem thêm ${roots.size - 3} bình luận",
                color = Color(0xFF737373),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CommentRow(
    comment: CommentDto,
    currentUserId: String,
    isReply: Boolean,
    onDeleteComment: (Int) -> Unit,
    onReply: (CommentDto) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(start = if (isReply) 54.dp else 12.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = comment.avatar,
            contentDescription = "Ảnh đại diện ${comment.username ?: "User"}",
            modifier = Modifier.size(if (isReply) 30.dp else 38.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.username ?: "User", style = MaterialTheme.typography.labelMedium)
                commentAge(comment.createdAt)?.let {
                    Text(it, Modifier.padding(start = 6.dp), color = Color(0xFF737373), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(comment.content, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Trả lời", Modifier.clickable { onReply(comment) }.padding(vertical = 3.dp), color = Color(0xFF737373), style = MaterialTheme.typography.labelSmall)
                if (comment.userId == currentUserId) {
                    Text("Xóa", Modifier.clickable { onDeleteComment(comment.id) }.padding(start = 14.dp, top = 3.dp, bottom = 3.dp), color = Color(0xFF737373), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun commentAge(value: String?): String? {
    if (value.isNullOrBlank()) return null
    return runCatching {
        val normalized = value.replace(' ', 'T')
        val created = runCatching { java.time.Instant.parse(normalized) }.getOrElse {
            runCatching { java.time.OffsetDateTime.parse(normalized).toInstant() }.getOrElse {
                java.time.LocalDateTime.parse(normalized.take(19))
                    .toInstant(java.time.ZoneOffset.UTC)
            }
        }
        val duration = java.time.Duration.between(created, java.time.Instant.now())
            .let { if (it.isNegative) java.time.Duration.ZERO else it }
        when {
            duration.toDays() >= 7 -> "${duration.toDays() / 7} tuần"
            duration.toDays() >= 1 -> "${duration.toDays()} ngày"
            duration.toHours() >= 1 -> "${duration.toHours()} giờ"
            duration.toMinutes() >= 1 -> "${duration.toMinutes()} phút"
            else -> "vừa xong"
        }
    }.getOrNull()
}
