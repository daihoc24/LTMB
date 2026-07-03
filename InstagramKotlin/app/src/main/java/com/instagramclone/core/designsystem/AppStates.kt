package com.instagramclone.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun StartupScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().testTag("startup-screen-container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Instagram", style = MaterialTheme.typography.headlineMedium)
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
    }
}

@Composable
fun BootstrapErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp).testTag("bootstrap-error-container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
        Text(message, modifier = Modifier.padding(vertical = 12.dp))
        Button(onClick = onRetry, modifier = Modifier.testTag("bootstrap-error-retry-button")) {
            Text("Retry")
        }
    }
}
