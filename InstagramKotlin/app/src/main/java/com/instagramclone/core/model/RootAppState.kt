package com.instagramclone.core.model

sealed interface RootAppState {
    data object Bootstrapping : RootAppState
    data object Unauthenticated : RootAppState
    data class Authenticated(val userKey: String? = null) : RootAppState
    data class BootstrapError(val message: String) : RootAppState
}
