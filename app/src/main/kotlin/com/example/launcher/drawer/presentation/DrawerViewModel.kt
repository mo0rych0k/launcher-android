package com.example.launcher.drawer.presentation

import androidx.lifecycle.ViewModel
import com.example.launcher.apps.domain.LauncherAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DrawerUiState(
    val query: String = "",
    val filteredApps: List<LauncherAppInfo> = emptyList()
)

class DrawerViewModel(
    private val allApps: List<LauncherAppInfo>
) : ViewModel() {
    private val _uiState = MutableStateFlow(DrawerUiState(filteredApps = allApps))
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        val filtered = allApps.filter { it.label.contains(query, ignoreCase = true) }
        _uiState.value = DrawerUiState(query = query, filteredApps = filtered)
    }
}
