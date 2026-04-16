package com.example.launcher.drawer

import android.content.Intent
import com.example.launcher.apps.domain.LauncherAppInfo
import com.example.launcher.drawer.presentation.DrawerViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerViewModelSearchTest {
    @Test
    fun query_filtersAppsCaseInsensitive() {
        val vm = DrawerViewModel(
            allApps = listOf(
                LauncherAppInfo("Clock", "clock.pkg", "Main", Intent(), null),
                LauncherAppInfo("Calendar", "calendar.pkg", "Main", Intent(), null)
            )
        )

        vm.onQueryChanged("clo")

        assertEquals(listOf("Clock"), vm.uiState.value.filteredApps.map { it.label })
    }
}
