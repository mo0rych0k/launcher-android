package com.example.launcher.apps.domain

import android.content.Intent

data class LauncherAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val launchIntent: Intent?
)
