package com.example.diceroller.models

data class YoutubeUsageQueueData(var dayOfWeek: String, var startTime: String) {

    var contentType: String = ""
    var videoName: String? = null
    var videoChannelName: String? = null
    var adName: String? = null
    var adSkipped: Integer? = null
    var endTime: String? = null
    var id: Int? = null
}
