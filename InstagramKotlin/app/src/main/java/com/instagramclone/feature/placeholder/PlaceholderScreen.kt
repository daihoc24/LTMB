package com.instagramclone.feature.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.instagramclone.core.model.MainTab

@Composable
fun PlaceholderScreen(
    tab: MainTab,
    modifier: Modifier = Modifier,
    onLogout: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().testTag("${tab.route}-screen-container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(tab.label, style = MaterialTheme.typography.headlineMedium)
        Text("This feature is coming in the next unit")
        onLogout?.let {
            Button(
                onClick = it,
                modifier = Modifier.padding(top = 24.dp).testTag("profile-logout-button"),
            ) {
                Text("Log out")
            }
        }
    }
}
