package com.instagramclone.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = InstagramBlue,
    background = InstagramBackground,
    surface = InstagramSurface,
    onBackground = InstagramText,
    onSurface = InstagramText,
    error = InstagramError,
)

private val DarkColors = darkColorScheme(
    primary = InstagramBlueDark,
)

@Composable
fun InstagramTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = InstagramTypography,
        content = content,
    )
}
