package com.example.insightsX.constants

object InstagramViewIdConstants {
    const val ACTION_BAR_TITLE = "action_bar_title"
    const val BUTTON_TEXT = "button_text"
    const val SPONSORED = "Sponsored"


    val profileViewIds = mutableListOf<String>(
        "row_profile_header_textview_followers_count",
        "row_profile_header_textview_following_count",
        "row_profile_header_textview_post_count",
        "change_avatar_button",
        "personal_information_entry_point",
        "gender_label",
        "birthday_label",
        "phone_label"
    )
    const val profileActionBarTitle = "Personal Information"

    val messagingViewIds = mutableListOf<String>(
        "row_inbox_container",
        "avatar_container"
    )

    val chatRoomViewIds = mutableListOf<String>(
        "thread_title",
        "thread_fragment_container"
    )


    // Also if actionBarTitle = "Room Setup"
    val roomsViewIds = mutableListOf<String>(
        "rooms_tab_suggested_thread_subtitle_view",
        "rooms_tab_section_header_text"
    )
    const val roomActionBarTitle = "Room Setup"


    // Message Requests if actionBarTitle = "MessageRequests
    const val messageRequestsActionBarTitle = "Message Requests"

    val storiesViewIds = mutableListOf<String>(
        "reel_viewer_timestamp",
        "reel_viewer_title",
    )
    const val storiesSponsoredViewId = "reel_viewer_subtitle"
    const val storiesAdCompany = "reel_viewer_title"




    val reelsViewIds = mutableListOf<String>(
        "comment_count",
        "like_count",
        "attribution_label"
    )
    const val reelsSponsoredViewId = "subtitle_text"
    const val reelsAdDescription = "video_caption"
    const val reelsAdCompany = "username"


    val instaBrowserViewIds = mutableListOf<String>(
        "ig_browser_text_title",
        "ig_browser_text_subtitle"
    )
    const val browserTitle = "ig_browser_text_title"
    const val browserDescription = "ig_browser_text_subtitle"


    val feedViewIds = mutableListOf<String>(
        "row_feed_photo_profile_name",
        "row_feed_textview_likes",
        "row_feed_photo_profile_name",
        "row_feed_comment_textview_layout"
    )
    const val postSponsoredViewId = "secondary_label"
    const val companyAdNameViewId = "row_feed_photo_profile_name"
    const val adDescriptionViewId = "row_feed_comment_textview_layout"
    const val adDescription2ViewId = "row_feed_headline"
}