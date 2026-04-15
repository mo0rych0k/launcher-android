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
    val dockApps: List<LauncherAppInfo> = emptyList()
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
        _uiState.value = HomeUiState(homeGridApps = appRepository.getLaunchableApps().take(20))
    }

    suspend fun pinToDock(app: LauncherAppInfo) {
        val updated = (_uiState.value.dockApps + app)
            .distinctBy { it.packageName }
            .take(5)
        _uiState.value = _uiState.value.copy(dockApps = updated)
        dockPrefsStore.saveDockPackages(updated.map { it.packageName })
    }
}
