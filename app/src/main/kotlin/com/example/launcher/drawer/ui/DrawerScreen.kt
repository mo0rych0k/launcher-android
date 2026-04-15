package com.example.launcher.drawer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.launcher.core.theme.ColorTokens
import com.example.launcher.drawer.presentation.DrawerUiState

@Composable
fun DrawerScreen(
    uiState: DrawerUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("drawer_screen")
    ) {
        uiState.filteredApps.forEach { app ->
            Text(text = app.label, color = ColorTokens.Text)
        }
    }
}
