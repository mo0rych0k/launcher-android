package com.example.launcher.apps.data

import com.example.launcher.apps.domain.LauncherAppInfo

interface AppRepository {
    suspend fun getLaunchableApps(): List<LauncherAppInfo>
}
