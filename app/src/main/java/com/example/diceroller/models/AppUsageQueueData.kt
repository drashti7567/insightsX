package com.example.diceroller.models;

data class AppUsageQueueData(
    var appPackageName: String,
    var appName: String,
    var dayofWeek: String,
    var startTime: String,
    var endTime: String?
)
