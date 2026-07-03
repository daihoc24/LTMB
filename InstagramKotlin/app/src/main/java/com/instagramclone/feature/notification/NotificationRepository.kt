package com.instagramclone.feature.notification

import com.instagramclone.data.remote.notification.NotificationDto

interface NotificationRepository { suspend fun load(): Result<List<NotificationDto>> }
