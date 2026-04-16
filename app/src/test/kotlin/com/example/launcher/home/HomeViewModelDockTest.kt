package com.example.launcher.home

import android.content.Intent
import com.example.launcher.apps.data.AppRepository
import com.example.launcher.apps.domain.LauncherAppInfo
import com.example.launcher.home.domain.DockPrefsStore
import com.example.launcher.home.presentation.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelDockTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun pinApp_updatesDockAndPersists() = runTest {
        val store = InMemoryDockPrefsStore()
        val vm = HomeViewModel(FakeAppRepository(), store)
        val app = LauncherAppInfo("Camera", "cam.pkg", "Main", Intent(), null)

        vm.pinToDock(app)

        assertTrue(vm.uiState.value.dockApps.any { it.packageName == "cam.pkg" })
        assertEquals(listOf("cam.pkg"), store.savedPackages)
    }

    @Test
    fun load_setsAlphabeticalAllAppsAndHomeSubset() = runTest {
        val apps = listOf(
            LauncherAppInfo("zeta", "z.pkg", "Main", Intent(), null),
            LauncherAppInfo("Alpha", "a.pkg", "Main", Intent(), null),
            LauncherAppInfo("beta", "b.pkg", "Main", Intent(), null)
        )
        val vm = HomeViewModel(FakeAppRepository(apps), InMemoryDockPrefsStore())

        vm.load().join()

        assertEquals(listOf("Alpha", "beta", "zeta"), vm.uiState.value.allApps.map { it.label })
        assertEquals(listOf("Alpha", "beta", "zeta"), vm.uiState.value.homeGridApps.map { it.label })
    }

    @Test
    fun onSearchQueryChange_updatesQuery() = runTest {
        val vm = HomeViewModel(FakeAppRepository(), InMemoryDockPrefsStore())

        vm.onSearchQueryChange(" cam ")

        assertEquals(" cam ", vm.uiState.value.searchQuery)
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
    constructor()
    constructor(apps: List<LauncherAppInfo>) {
        launchableApps = apps
    }

    private var launchableApps: List<LauncherAppInfo> = emptyList()

    override suspend fun getLaunchableApps(): List<LauncherAppInfo> = launchableApps
}
