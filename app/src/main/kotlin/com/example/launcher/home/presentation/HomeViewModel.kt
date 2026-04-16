package com.example.launcher.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.apps.data.AppRepository
import com.example.launcher.apps.domain.LauncherAppInfo
import com.example.launcher.home.domain.DockPrefsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val homeGridApps: List<LauncherAppInfo> = emptyList(),
    val dockApps: List<LauncherAppInfo> = emptyList(),
    val allApps: List<LauncherAppInfo> = emptyList(),
    val searchQuery: String = ""
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val dockPrefsStore: DockPrefsStore = object : DockPrefsStore {
        override suspend fun loadDockPackages(): List<String> = emptyList()
        override suspend fun saveDockPackages(packageNames: List<String>) = Unit
    }
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        val apps = appRepository.getLaunchableApps().sortedBy { it.label.lowercase() }
        _uiState.value = _uiState.value.copy(
            homeGridApps = apps.take(20),
            allApps = apps
        )
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    suspend fun pinToDock(app: LauncherAppInfo) {
        val updated = (_uiState.value.dockApps + app)
            .distinctBy { it.packageName }
            .take(5)
        _uiState.value = _uiState.value.copy(dockApps = updated)
        dockPrefsStore.saveDockPackages(updated.map { it.packageName })
    }
}
