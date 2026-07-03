package com.instagramclone.data.repository

import com.instagramclone.core.network.ApiResult
import com.instagramclone.core.session.CurrentUser
import com.instagramclone.core.session.SessionCleanup
import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import com.instagramclone.core.session.SessionStore
import com.instagramclone.core.session.SessionTokenProvider
import com.instagramclone.data.remote.user.UserRemoteDataSource
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.JvmSuppressWildcards
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class DefaultSessionRepository @Inject constructor(
    private val store: SessionStore,
    private val tokenProvider: SessionTokenProvider,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val cleanupHooks: Set<@JvmSuppressWildcards SessionCleanup>,
) : SessionRepository {
    private val mutex = Mutex()
    private val epoch = AtomicLong(0)
    private val _state = MutableStateFlow<SessionState>(SessionState.Checking)
    override val state: StateFlow<SessionState> = _state.asStateFlow()
    private var currentUser: CurrentUser? = null

    override suspend fun restore(): SessionState {
        val snapshot = mutex.withLock {
            val restoreEpoch = epoch.get()
            _state.value = SessionState.Checking
            val token = store.readToken()
            if (epoch.get() != restoreEpoch) return@withLock null
            if (token.isNullOrBlank()) {
                tokenProvider.update(null)
                currentUser = null
                _state.value = SessionState.Unauthenticated
                return@withLock null
            }
            tokenProvider.update(token)
            RestoreSnapshot(restoreEpoch)
        } ?: return _state.value

        val result = userRemoteDataSource.getMyInfo()
        return mutex.withLock {
            if (epoch.get() != snapshot.epoch) return@withLock _state.value
            val nextState = when (result) {
            is ApiResult.Success -> SessionState.Authenticated(result.value).also { currentUser = result.value }
            is ApiResult.Unauthorized -> {
                clearSessionLocked()
                SessionState.Unauthenticated
            }
            is ApiResult.BusinessError -> recoverable(result.userMessage)
            is ApiResult.ClientError -> recoverable(result.userMessage)
            is ApiResult.DataError -> recoverable(result.userMessage)
            is ApiResult.NetworkError -> recoverable(result.userMessage)
            is ApiResult.ServerError -> recoverable(result.userMessage)
            is ApiResult.UncategorizedError -> recoverable(result.userMessage)
            }
            _state.value = nextState
            nextState
        }
    }

    override suspend fun establishSession(token: String): SessionState {
        mutex.withLock {
            epoch.incrementAndGet()
            store.saveToken(token)
            tokenProvider.update(token)
            _state.value = SessionState.Checking
        }
        return restore()
    }

    override suspend fun logout() {
        mutex.withLock {
            epoch.incrementAndGet()
            clearSessionLocked()
            _state.value = SessionState.Unauthenticated
        }
    }

    private fun recoverable(message: String) = SessionState.RecoverableError(
        message = message,
        hasStoredToken = true,
    )

    private suspend fun clearSessionLocked() {
        tokenProvider.update(null)
        currentUser = null
        store.clear()
        cleanupHooks.forEach { hook -> runCatching { hook.onSessionCleared() } }
    }

    private data class RestoreSnapshot(val epoch: Long)
}
