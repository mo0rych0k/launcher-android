package com.example.launcher.apps

import com.example.launcher.apps.data.InstalledAppsSource
import com.example.launcher.apps.data.PackageManagerAppRepository
import com.example.launcher.apps.domain.LauncherAppInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PackageManagerAppRepositoryTest {
    @Test
    fun returnsOnlyLaunchableApps_sortedByLabel() = runTest {
        val fakeSource = FakeInstalledAppsSource(
            listOf(
                LauncherAppInfo("Zeta", "z.pkg", "Main", android.content.Intent(), null),
                LauncherAppInfo("Alpha", "a.pkg", "Main", android.content.Intent(), null),
                LauncherAppInfo("Hidden", "h.pkg", "Main", null, null)
            )
        )
        val repo = PackageManagerAppRepository(fakeSource)

        val result = repo.getLaunchableApps()

        assertEquals(listOf("Alpha", "Zeta"), result.map { it.label })
    }
}

private class FakeInstalledAppsSource(
    private val apps: List<LauncherAppInfo>
) : InstalledAppsSource {
    override suspend fun queryApps(): List<LauncherAppInfo> = apps
}
