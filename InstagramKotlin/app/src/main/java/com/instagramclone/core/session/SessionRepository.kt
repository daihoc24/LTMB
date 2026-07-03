package com.instagramclone.core.session

import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val state: StateFlow<SessionState>
    suspend fun restore(): SessionState
    suspend fun establishSession(token: String): SessionState
    suspend fun logout()
}
