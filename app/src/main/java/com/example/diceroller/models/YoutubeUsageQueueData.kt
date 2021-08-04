package com.example.diceroller.models

data class YoutubeUsageQueueData(
    var contentType: String,
    var videoName: String?,
    var videoChannelName: String?,
    var adName: String?,
    var adSkippped: Boolean?,
    var startTime: String,
    var endTime: String?
)
