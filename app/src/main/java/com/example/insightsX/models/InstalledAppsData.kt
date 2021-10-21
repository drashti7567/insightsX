package com.example.insightsX.models

data class InstalledAppsData(
    var appName: String,
    var packageName: String,
    var isSystemPackage: Boolean?
)
