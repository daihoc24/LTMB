package com.instagramclone.data.repository

import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import com.instagramclone.data.remote.notification.NotificationApi
import com.instagramclone.data.remote.notification.NotificationDto
import com.instagramclone.feature.notification.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNotificationRepository @Inject constructor(
    private val api: NotificationApi,
    private val session: SessionRepository,
) : NotificationRepository {
    override suspend fun load(): Result<List<NotificationDto>> {
        return try {
            val id = (session.state.value as? SessionState.Authenticated)?.user?.id
                ?: return Result.failure(IllegalStateException("Phiên đăng nhập không hợp lệ"))
            val response = api.list(id)
            if (response.isSuccessful) Result.success(response.body().orEmpty())
            else Result.failure(IllegalStateException("Không tải được thông báo"))
        } catch (_: Throwable) { Result.failure(IllegalStateException("Không thể kết nối máy chủ")) }
    }
}
