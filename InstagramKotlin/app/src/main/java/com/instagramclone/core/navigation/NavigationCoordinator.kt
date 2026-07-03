package com.instagramclone.core.navigation

import com.instagramclone.core.model.MainTab
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class NavigationCoordinator @Inject constructor() {
    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: MainTab): TabSelection {
        val result = if (_selectedTab.value == tab) {
            TabSelection.Reselected(tab)
        } else {
            _selectedTab.value = tab
            TabSelection.Selected(tab)
        }
        return result
    }
}

sealed interface TabSelection {
    val tab: MainTab
    data class Selected(override val tab: MainTab) : TabSelection
    data class Reselected(override val tab: MainTab) : TabSelection
}
