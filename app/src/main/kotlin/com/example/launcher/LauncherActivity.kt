package com.example.launcher

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.launcher.apps.ui.AllAppsScreen
import com.example.launcher.apps.data.AndroidInstalledAppsSource
import com.example.launcher.apps.data.PackageManagerAppRepository
import com.example.launcher.core.theme.LauncherTheme
import com.example.launcher.home.presentation.HomeViewModel
import com.example.launcher.home.ui.HomeScreen

class LauncherActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
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
            val pagerState = rememberPagerState(initialPage = 0) { 2 }
            LauncherTheme {
                HorizontalPager(state = pagerState) { page ->
                    if (page == 0) {
                        HomeScreen(uiState = uiState)
                    } else {
                        AllAppsScreen(
                            uiState = uiState,
                            onQueryChange = viewModel::onSearchQueryChange
                        )
                    }
                }
            }
        }
    }
}
