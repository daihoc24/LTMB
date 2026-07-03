package com.instagramclone.core.model

enum class MainTab(
    val route: String,
    val label: String,
) {
    HOME("home", "Home"),
    SEARCH("search", "Search"),
    CREATE_POST("create-post", "Post"),
    NOTIFICATIONS("notifications", "Notifications"),
    PROFILE("profile", "Profile"),
}
