package com.example.insightsX.models

data class InstagramUsageQueueData(var dayOfWeek: String, var startTime: String) {
    var contentType: String = ""
    var endTime: String? = null
    var id: Int? = null
}
