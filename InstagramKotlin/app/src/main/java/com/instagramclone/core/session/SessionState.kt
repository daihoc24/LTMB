package com.instagramclone.core.session

sealed interface SessionState {
    data object Checking : SessionState
    data class Authenticated(val user: CurrentUser) : SessionState
    data object Unauthenticated : SessionState
    data class RecoverableError(
        val message: String,
        val hasStoredToken: Boolean,
    ) : SessionState
}
