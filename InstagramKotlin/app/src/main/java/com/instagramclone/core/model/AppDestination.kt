package com.instagramclone.core.model

sealed interface AppDestination {
    val route: String

    data class Main(val tab: MainTab) : AppDestination {
        override val route: String = tab.route
    }

    data object SignIn : AppDestination {
        override val route: String = "sign-in"
    }

    data class Post(val postId: Long) : AppDestination {
        init { require(postId > 0) }
        override val route: String = "post/$postId"
    }

    data class UserProfile(val userId: Long) : AppDestination {
        init { require(userId > 0) }
        override val route: String = "profile/$userId"
    }

    data class Conversation(val conversationId: Long) : AppDestination {
        init { require(conversationId > 0) }
        override val route: String = "conversation/$conversationId"
    }

    data class Camera(val requestKey: String) : AppDestination {
        init { require(requestKey.isNotBlank()) }
        override val route: String = "camera/$requestKey"
    }
}

data class InvalidDestination(
    val reason: Reason,
    val fallback: AppDestination.Main,
    val notifyUser: Boolean = true,
) {
    enum class Reason { MISSING_ARGUMENT, INVALID_ARGUMENT, UNSUPPORTED_ROUTE }
}

sealed interface DestinationResolution {
    data class Valid(val destination: AppDestination) : DestinationResolution
    data class Invalid(val value: InvalidDestination) : DestinationResolution
}
