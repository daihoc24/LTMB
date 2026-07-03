package com.instagramclone.core.model

sealed interface NavigationCommand {
    data class SelectTab(val tab: MainTab) : NavigationCommand
    data class Open(val destination: AppDestination) : NavigationCommand
    data object Back : NavigationCommand
    data class ResetRoot(val state: RootAppState) : NavigationCommand
}

sealed interface NavigationResult {
    val requestKey: String

    data class MediaSelected(
        override val requestKey: String,
        val uri: String,
    ) : NavigationResult

    data class Cancelled(override val requestKey: String) : NavigationResult
}

data class UiMessage(
    val id: Long,
    val text: String,
)
