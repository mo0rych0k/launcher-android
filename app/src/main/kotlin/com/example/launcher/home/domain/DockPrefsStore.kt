package com.example.launcher.home.domain

interface DockPrefsStore {
    suspend fun loadDockPackages(): List<String>
    suspend fun saveDockPackages(packageNames: List<String>)
}
