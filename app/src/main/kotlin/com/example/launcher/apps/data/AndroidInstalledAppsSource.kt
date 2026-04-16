package com.example.launcher.apps.data

import android.content.Intent
import android.content.pm.PackageManager
import com.example.launcher.apps.domain.LauncherAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidInstalledAppsSource(
    private val packageManager: PackageManager
) : InstalledAppsSource {
    override suspend fun queryApps(): List<LauncherAppInfo> = withContext(Dispatchers.IO) {
        val queryIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = packageManager.queryIntentActivities(queryIntent, 0)

        resolveInfos.map { info ->
            val label = info.loadLabel(packageManager).toString()
            val packageName = info.activityInfo.packageName
            val activityName = info.activityInfo.name
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            LauncherAppInfo(
                label = label,
                packageName = packageName,
                activityName = activityName,
                launchIntent = launchIntent
            )
        }
    }
}
