package com.instagramclone.core.navigation

import com.instagramclone.core.model.AppDestination
import com.instagramclone.core.model.DestinationResolution
import com.instagramclone.core.model.InvalidDestination
import com.instagramclone.core.model.MainTab
import javax.inject.Inject

class DestinationParser @Inject constructor() {
    fun parse(route: String?, arguments: Map<String, String> = emptyMap()): DestinationResolution {
        val fallback = AppDestination.Main(MainTab.HOME)
        return when (route) {
            MainTab.HOME.route -> DestinationResolution.Valid(fallback)
            MainTab.SEARCH.route -> DestinationResolution.Valid(AppDestination.Main(MainTab.SEARCH))
            MainTab.CREATE_POST.route -> DestinationResolution.Valid(AppDestination.Main(MainTab.CREATE_POST))
            MainTab.NOTIFICATIONS.route -> DestinationResolution.Valid(AppDestination.Main(MainTab.NOTIFICATIONS))
            MainTab.PROFILE.route -> DestinationResolution.Valid(AppDestination.Main(MainTab.PROFILE))
            "post" -> positiveId(arguments["postId"])?.let { DestinationResolution.Valid(AppDestination.Post(it)) }
                ?: invalid(InvalidDestination.Reason.INVALID_ARGUMENT, fallback)
            "user-profile" -> positiveId(arguments["userId"])?.let { DestinationResolution.Valid(AppDestination.UserProfile(it)) }
                ?: invalid(InvalidDestination.Reason.INVALID_ARGUMENT, fallback)
            "conversation" -> positiveId(arguments["conversationId"])?.let { DestinationResolution.Valid(AppDestination.Conversation(it)) }
                ?: invalid(InvalidDestination.Reason.INVALID_ARGUMENT, fallback)
            null, "" -> invalid(InvalidDestination.Reason.MISSING_ARGUMENT, fallback)
            else -> invalid(InvalidDestination.Reason.UNSUPPORTED_ROUTE, fallback)
        }
    }

    private fun positiveId(raw: String?): Long? = raw?.toLongOrNull()?.takeIf { it > 0 }

    private fun invalid(
        reason: InvalidDestination.Reason,
        fallback: AppDestination.Main,
    ) = DestinationResolution.Invalid(InvalidDestination(reason, fallback))
}
