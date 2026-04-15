package com.example.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.launcher.apps.data.AndroidInstalledAppsSource
import com.example.launcher.apps.data.PackageManagerAppRepository
import com.example.launcher.core.theme.LauncherTheme
import com.example.launcher.home.presentation.HomeViewModel
import com.example.launcher.home.ui.HomeScreen

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = remember {
                HomeViewModel(
                    appRepository = PackageManagerAppRepository(
                        source = AndroidInstalledAppsSource(packageManager)
                    )
                )
            }
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(viewModel) {
                viewModel.load()
            }
            LauncherTheme {
                HomeScreen(uiState = uiState)
            }
        }
    }
}
