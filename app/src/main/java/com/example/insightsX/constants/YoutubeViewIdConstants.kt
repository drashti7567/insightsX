package com.example.insightsX.constants

object YoutubeViewIdConstants {
    const val YOUTUBE_VIDEO_NAME_ID = "title"
    const val YOUTUBE_VIDEO_FULL_SCREEN_NAME_ID = "player_video_title_view"
    const val YOUTUBE_FLOATY_VIDEO_NAME_ID = "floaty_title"
    const val YOUTUBE_CHANNEL_NAME = "channel_title"
    const val YOUTUBE_FLOATY_CHANNEL_NAME = "floaty_subtitle"
    const val AD_NAME = "ad_progress_text"
    const val YOUTUBE_FLOATY_AD_NAME = "floaty_title"
    const val LIVE_CHAT_VEM = "live_chat_vem_text"
    const val LIVE_LABEL = "time_bar_live_label"

    val youtubeVideoIds = mutableListOf<String>("watch_player")
    val youtubeAdIds = mutableListOf<String>(
        "ad_progress_text",
        "ad_countdown",
        "expanded_details_title",
        "skip_ad_button"
    )
    val floatyVideoIds = mutableListOf<String>("floaty_title", "floaty_subtitle")
    val floatyAdsIds = mutableListOf<String>("ad_badge")
    val reelsIds = mutableListOf<String>(
        "reel_main_title",
        "reel_byline_text",
        "reel_back_button",
        "reel_progress_bar"
    )
}