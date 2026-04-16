package com.example.launcher.apps.domain

import android.content.Intent
import android.graphics.drawable.Drawable

data class LauncherAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val launchIntent: Intent?,
    val icon: Drawable?
)
