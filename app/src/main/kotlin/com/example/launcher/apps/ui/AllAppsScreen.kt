package com.example.launcher.apps.ui

import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.launcher.home.presentation.HomeUiState

@Composable
fun AllAppsScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedQuery = uiState.searchQuery.trim()
    val filteredApps = remember(uiState.allApps, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            uiState.allApps
        } else {
            uiState.allApps.filter { app ->
                app.label.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("all_apps_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search apps") },
            singleLine = true
        )

        if (filteredApps.isEmpty()) {
            Text(text = "Nothing found")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val context = LocalContext.current
                        AndroidView(
                            factory = { ImageView(context) },
                            update = { imageView ->
                                imageView.setImageDrawable(app.icon)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Text(text = app.label)
                    }
                }
            }
        }
    }
}
