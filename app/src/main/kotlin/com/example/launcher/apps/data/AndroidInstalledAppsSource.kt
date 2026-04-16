package com.example.launcher.apps.data

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.launcher.apps.domain.LauncherAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidInstalledAppsSource(
    private val packageManager: PackageManager
) : InstalledAppsSource {
    override suspend fun queryApps(): List<LauncherAppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null )
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of( 0L )
            )
        } else {
            packageManager.queryIntentActivities(mainIntent, 0 )
        }

        resolvedInfos.mapNotNull { resolveInfo ->
            val label = resolveInfo.loadLabel(packageManager).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val resources = packageManager.getResourcesForApplication(
                resolveInfo.activityInfo.applicationInfo
            )

            val appName = if (resolveInfo.labelRes != 0) {
                resources.getString(resolveInfo.labelRes)
            } else {
                resolveInfo.loadLabel(packageManager).toString()
            }

            val launchIntent = packageManager.getLaunchIntentForPackage(
                packageName
            ) ?: return@mapNotNull null

            val icon = resolveInfo.activityInfo.loadIcon(packageManager)

            LauncherAppInfo(
                label = label,
                packageName = packageName,
                activityName = appName,
                launchIntent = launchIntent,
                icon = icon
            )
        }
    }
}
