package com.example.diceroller.constants;

object FileNameConstants {
    const val APP_USAGE_FILE_NAME = "AppUsage.csv"
    const val YOUTUBE_USAGE_FILE_NAME = "YoutubeUsage.csv"
    const val SYSTEM_LOGS_FILE_NAME = "AccessibilityService.csv"

    const val APP_USAGE_FILE_HEADERS = "Package Name, App Name, Day Of Week, Start Time, End Time\n"
    const val YOUTUBE_USAGE_FILE_HEADERS = "Content Type, Video Name, Video Channel," +
            " Day Of Week, Start Time, End Time\n"
    const val SYSTEM_LOGS_FILE_HEADERS = "Time, System Event\n"
}
