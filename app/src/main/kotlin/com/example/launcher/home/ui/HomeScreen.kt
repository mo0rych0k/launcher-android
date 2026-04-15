package com.example.launcher.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.launcher.clock.ClockStateProducer
import com.example.launcher.clock.ui.PixelClock
import com.example.launcher.core.theme.ColorTokens
import com.example.launcher.home.presentation.HomeUiState
import com.example.launcher.widgets.ui.WidgetHostArea

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    val clockText = ClockStateProducer().nowText()
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PixelClock(text = clockText)
        WidgetHostArea()
        uiState.homeGridApps.forEach { app ->
            Text(text = app.label, color = ColorTokens.Text)
        }
    }
}
