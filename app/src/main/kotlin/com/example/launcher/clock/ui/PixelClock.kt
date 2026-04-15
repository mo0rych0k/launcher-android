package com.example.launcher.clock.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily

@Composable
fun PixelClock(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.testTag("pixel_clock"),
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
}
