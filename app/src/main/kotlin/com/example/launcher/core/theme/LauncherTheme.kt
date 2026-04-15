package com.example.launcher.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ColorTokens.Bg,
            primary = ColorTokens.Accent,
            onBackground = ColorTokens.Text
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorTokens.Bg)
                .testTag("launcher_theme_root")
        ) {
            content()
        }
    }
}
