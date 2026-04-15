package com.example.launcher.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.apps.data.AppRepository
import com.example.launcher.apps.domain.LauncherAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val homeGridApps: List<LauncherAppInfo> = emptyList()
)

class HomeViewModel(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.value = HomeUiState(homeGridApps = appRepository.getLaunchableApps().take(20))
    }
}
