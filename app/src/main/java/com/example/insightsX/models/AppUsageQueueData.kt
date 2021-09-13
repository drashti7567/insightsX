package com.example.insightsX.models;

data class AppUsageQueueData(
    var appPackageName: String,
    var appName: String,
    var dayOfWeek: String,
    var startTime: String,
    var endTime: String?
) {
    var id: Number? = null
}
