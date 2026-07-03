package com.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.instagramclone.app.AppStateCoordinator
import com.instagramclone.app.InstagramApp
import com.instagramclone.core.navigation.NavigationCoordinator
import com.instagramclone.core.navigation.UiMessageManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appStateCoordinator: AppStateCoordinator
    @Inject lateinit var navigationCoordinator: NavigationCoordinator
    @Inject lateinit var uiMessageManager: UiMessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstagramApp(
                appStateCoordinator = appStateCoordinator,
                navigationCoordinator = navigationCoordinator,
                uiMessageManager = uiMessageManager,
            )
        }
    }
}
