package com.example.launcher.widgets.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun WidgetHostArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(160.dp)
            .fillMaxWidth()
            .border(2.dp, Color.Red)
            .testTag("widget_host_area")
    )
}
