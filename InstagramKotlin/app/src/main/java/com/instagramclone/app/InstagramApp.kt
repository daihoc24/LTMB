package com.instagramclone.app

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instagramclone.core.designsystem.BootstrapErrorScreen
import com.instagramclone.core.designsystem.InstagramTheme
import com.instagramclone.core.designsystem.StartupScreen
import com.instagramclone.core.model.RootAppState
import com.instagramclone.core.navigation.MainNavigation
import com.instagramclone.core.navigation.NavigationCoordinator
import com.instagramclone.core.navigation.UiMessageManager
import com.instagramclone.feature.auth.navigation.AuthNavigation

@Composable
fun InstagramApp(
    appStateCoordinator: AppStateCoordinator,
    navigationCoordinator: NavigationCoordinator,
    uiMessageManager: UiMessageManager,
) {
    val rootState by appStateCoordinator.state.collectAsStateWithLifecycle()
    val message by uiMessageManager.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message?.id) {
        message?.let {
            snackbarHostState.showSnackbar(it.text)
            uiMessageManager.consume(it.id)
        }
    }

    InstagramTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
            when (val state = rootState) {
                RootAppState.Bootstrapping -> StartupScreen()
                RootAppState.Unauthenticated -> AuthNavigation(showMessage = uiMessageManager::show)
                is RootAppState.Authenticated -> MainNavigation(
                    navigationCoordinator = navigationCoordinator,
                    contentPadding = contentPadding,
                    onLogout = appStateCoordinator::logout,
                )
                is RootAppState.BootstrapError -> BootstrapErrorScreen(
                    message = state.message,
                    onRetry = appStateCoordinator::retryBootstrap,
                )
            }
        }
    }
}
