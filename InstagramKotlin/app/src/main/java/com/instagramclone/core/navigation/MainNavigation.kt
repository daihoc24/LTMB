package com.instagramclone.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.instagramclone.core.model.MainTab
import com.instagramclone.feature.placeholder.PlaceholderScreen
import com.instagramclone.feature.media.CreateMediaScreen
import com.instagramclone.feature.content.FeedScreen
import com.instagramclone.feature.social.SearchScreen
import com.instagramclone.feature.social.ProfileScreen
import com.instagramclone.feature.notification.NotificationScreen
import com.instagramclone.feature.chat.ChatScreen

@Composable
fun MainNavigation(
    navigationCoordinator: NavigationCoordinator,
    contentPadding: PaddingValues,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val selectedTab by navigationCoordinator.selectedTab.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun selectTab(tab: MainTab) {
        when (navigationCoordinator.selectTab(tab)) {
            is TabSelection.Reselected -> navController.popBackStack(tab.route, inclusive = false)
            is TabSelection.Selected -> navController.navigate(tab.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    BackHandler(enabled = currentRoute in MainTab.entries.map { it.route } && currentRoute != MainTab.HOME.route) {
        selectTab(MainTab.HOME)
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        bottomBar = {
            if (currentRoute in MainTab.entries.map { it.route }) {
                InstagramBottomBar(
                    selectedTab = MainTab.entries.firstOrNull { it.route == currentRoute } ?: selectedTab,
                    onTabSelected = ::selectTab,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            MainTab.entries.forEach { tab ->
                composable(tab.route) {
                    if (tab == MainTab.HOME) {
                        FeedScreen(
                            onOpenChat = { navController.navigate("chat") },
                            onUserClick = { navController.navigate("profile/$it") },
                        )
                    } else if (tab == MainTab.SEARCH) {
                        SearchScreen(
                            onPostClick = { postId -> navController.navigate("post/$postId") },
                            onUserClick = { userId -> navController.navigate("profile/$userId") },
                        )
                    } else if (tab == MainTab.CREATE_POST) {
                        CreateMediaScreen()
                    } else if (tab == MainTab.NOTIFICATIONS) {
                        NotificationScreen()
                    } else if (tab == MainTab.PROFILE) {
                        ProfileScreen(
                            onLogout = onLogout,
                            onPostClick = { postId -> navController.navigate("post/$postId") },
                        )
                    } else {
                        PlaceholderScreen(
                            tab = tab,
                            onLogout = onLogout.takeIf { tab == MainTab.PROFILE },
                        )
                    }
                }
            }
            composable("chat") { ChatScreen(onClose = { navController.popBackStack() }) }
            composable("profile/{userId}") {
                ProfileScreen(
                    onLogout = onLogout,
                    onPostClick = { postId -> navController.navigate("post/$postId") },
                    onBack = { navController.popBackStack() },
                    onMessage = { navController.navigate("chat") },
                )
            }
            composable("post/{postId}") { entry ->
                val postId = entry.arguments?.getString("postId")?.toIntOrNull()
                FeedScreen(
                    onOpenChat = { navController.navigate("chat") },
                    onUserClick = { navController.navigate("profile/$it") },
                    focusPostId = postId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun InstagramBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationBar(modifier = Modifier.testTag("main-bottom-navigation"), tonalElevation = 0.dp) {
        MainTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val icons = tabIcons(tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) icons.first else icons.second,
                        contentDescription = tab.label,
                    )
                },
                label = null,
                alwaysShowLabel = false,
                modifier = Modifier.testTag("main-tab-${tab.route}-button"),
            )
        }
    }
}

private fun tabIcons(tab: MainTab): Pair<ImageVector, ImageVector> = when (tab) {
    MainTab.HOME -> Icons.Filled.Home to Icons.Outlined.Home
    MainTab.SEARCH -> Icons.Filled.Search to Icons.Outlined.Search
    MainTab.CREATE_POST -> Icons.Filled.AddBox to Icons.Outlined.AddBox
    MainTab.NOTIFICATIONS -> Icons.Filled.Notifications to Icons.Outlined.Notifications
    MainTab.PROFILE -> Icons.Filled.Person to Icons.Outlined.Person
}
