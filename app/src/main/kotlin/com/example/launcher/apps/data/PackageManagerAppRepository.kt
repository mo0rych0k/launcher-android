package com.example.launcher.apps.data

import com.example.launcher.apps.domain.LauncherAppInfo

interface InstalledAppsSource {
    suspend fun queryApps(): List<LauncherAppInfo>
}

class PackageManagerAppRepository(
    private val source: InstalledAppsSource
) : AppRepository {
    override suspend fun getLaunchableApps(): List<LauncherAppInfo> {
        return source.queryApps()
            .filter { it.launchIntent != null }
            .sortedBy { it.label.lowercase() }
    }
}
