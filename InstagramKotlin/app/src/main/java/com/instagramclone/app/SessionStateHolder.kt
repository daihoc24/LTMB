package com.instagramclone.app

import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Singleton
class SessionStateHolder @Inject constructor(
    private val repository: SessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val state: StateFlow<SessionState> = repository.state

    init {
        restore()
    }

    fun restore() {
        scope.launch { repository.restore() }
    }

    fun retry() {
        if (state.value is SessionState.RecoverableError) restore()
    }

    fun logout() {
        scope.launch { repository.logout() }
    }
}
