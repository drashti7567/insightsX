package com.example.diceroller.models

data class YoutubeUsageQueueData(var dayOfWeek: String, var startTime: String) {
    var contentType: String = ""
    var videoName: String? = null
    var videoChannelName: String? = null
    var adName: String? = null
    var adSkippped: Boolean? = null
    var endTime: String? = null
}
