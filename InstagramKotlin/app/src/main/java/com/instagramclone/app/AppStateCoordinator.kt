package com.instagramclone.app

import com.instagramclone.core.model.RootAppState
import com.instagramclone.core.session.SessionState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@Singleton
class AppStateCoordinator @Inject constructor(
    private val sessionStateHolder: SessionStateHolder,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val state: StateFlow<RootAppState> = sessionStateHolder.state
        .map { sessionState ->
            when (sessionState) {
                SessionState.Checking -> RootAppState.Bootstrapping
                is SessionState.Authenticated -> RootAppState.Authenticated(sessionState.user.id)
                SessionState.Unauthenticated -> RootAppState.Unauthenticated
                is SessionState.RecoverableError -> RootAppState.BootstrapError(sessionState.message)
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, RootAppState.Bootstrapping)

    fun retryBootstrap() {
        sessionStateHolder.retry()
    }

    fun logout() {
        sessionStateHolder.logout()
    }
}
