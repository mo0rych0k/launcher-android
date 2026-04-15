package com.example.launcher.home

import android.content.Intent
import com.example.launcher.apps.data.AppRepository
import com.example.launcher.apps.domain.LauncherAppInfo
import com.example.launcher.home.domain.DockPrefsStore
import com.example.launcher.home.presentation.HomeViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelDockTest {
    @Test
    fun pinApp_updatesDockAndPersists() = runTest {
        val store = InMemoryDockPrefsStore()
        val vm = HomeViewModel(FakeAppRepository(), store)
        val app = LauncherAppInfo("Camera", "cam.pkg", "Main", Intent())

        vm.pinToDock(app)

        assertTrue(vm.uiState.value.dockApps.any { it.packageName == "cam.pkg" })
        assertEquals(listOf("cam.pkg"), store.savedPackages)
    }
}

private class InMemoryDockPrefsStore : DockPrefsStore {
    var savedPackages: List<String> = emptyList()

    override suspend fun loadDockPackages(): List<String> = savedPackages

    override suspend fun saveDockPackages(packageNames: List<String>) {
        savedPackages = packageNames
    }
}

private class FakeAppRepository : AppRepository {
    override suspend fun getLaunchableApps(): List<LauncherAppInfo> = emptyList()
}
