package com.example.diceroller

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.constants.YoutubeViewIdConstants
import com.example.diceroller.tracker.AppTracker
import com.example.diceroller.utils.FileUtils
import kotlinx.coroutines.*

class MyAccessibilityService : AccessibilityService() {

    var parentElementLayout: AccessibilityNodeInfo? = null
    lateinit var listOfViewIds: MutableList<String>;
    lateinit var mapOfViewIdsWithText: HashMap<String, String>;
    var allowWindowContentChangeEvent: Boolean = false

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



    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "Service Connected")

        val appChecker: AppChecker = AppChecker();
        FileUtils.emptyFileContents(this, FileNameConstants.APP_USAGE_FILE_NAME)
        AppTracker.startAppTracker(appChecker, this)
    }

    @SuppressLint("NewApi")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (!filterBasedOnAppPackageName(event)) return
        if (event.eventType === AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && !allowWindowContentChangeEvent) return
        if (event.eventType == 4096) return
        if (event.eventType == AccessibilityEvent.TYPE_ANNOUNCEMENT) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            this.allowWindowContentChangeEvent = true
        }


        val source: AccessibilityNodeInfo = event.source ?: return;
        this.initializeViewIdsList()

        getParent(source)
        getListOfViewIds(this.parentElementLayout)
        printYoutubeContentType()
    }

    private fun filterBasedOnAppPackageName(event: AccessibilityEvent): Boolean {
        if (event.packageName != null && event.packageName.equals("com.google.android.youtube")) return true;
        return false
    }

    private fun printYoutubeContentType() {
        if (this.listOfViewIds.containsAll(this.floatyVideoIds) && !this.listOfViewIds.containsAll(
                this.floatyAdsIds
            )
        ) {
            Log.d("VideoType", "Floaty Youtube Video")
            Log.d("VideoName", this.mapOfViewIdsWithText.get(YoutubeViewIdConstants.YOUTUBE_FLOATY_VIDEO_NAME_ID) ?: "")
            Log.d("ChannelName", this.mapOfViewIdsWithText.get(YoutubeViewIdConstants.YOUTUBE_FLOATY_CHANNEL_NAME) ?: "")
            Handler().postDelayed({
                this.allowWindowContentChangeEvent = false
            }, 1000)

        } else if (this.listOfViewIds.containsAll(this.floatyAdsIds) && this.listOfViewIds.containsAll(
                this.floatyVideoIds
            )
        ) {
            Log.d("Video Type", "Floaty Ad")

        } else if (this.listOfViewIds.any { it in this.youtubeAdIds }) {
            Log.d("VideoType", "Youtube Ads")
        } else if (this.listOfViewIds.containsAll(this.youtubeVideoIds)) {
            Log.d("VideoType", "Youtube Video")
            Log.d("VideoName", this.mapOfViewIdsWithText.get(YoutubeViewIdConstants.YOUTUBE_VIDEO_NAME_ID) ?: "")
            Log.d("ChannelName", this.mapOfViewIdsWithText.get(YoutubeViewIdConstants.YOUTUBE_CHANNEL_NAME) ?: "")
            Handler().postDelayed({
                this.allowWindowContentChangeEvent = false
            }, 1000)
        } else if (this.listOfViewIds.any { it in this.reelsIds }) {
            Log.d("VideoType", "Reels")
            this.allowWindowContentChangeEvent = false
        } else {
            Log.d("Video Type", "Browse Content")
            this.allowWindowContentChangeEvent = false
        }
        Log.d("TAG", "-----------------------------------------------------------------------------------' ")
    }

    private fun printTypeOfEvent(event: AccessibilityEvent) {
        var eventTypeString: String;
        when (event.eventType) {
            1 -> eventTypeString = "TYPE_VIEW_CLICKED + WINDOWS_CHANGE_ADDED"
            4 -> eventTypeString = "TYPE_VIEW_SELECTED + WINDOWS_CHANGE_TITLE"
            8 -> eventTypeString = "WINDOWS_CHANGE_BOUNDS"
            16 -> eventTypeString = "WINDOWS_CHANGE_LAYER"
            32 -> eventTypeString = "WINDOWS_CHANGE_ACTIVE"
            2048 -> eventTypeString = "TYPE_WINDOW_CONTENT_CHANGED"
            4096 -> eventTypeString = "TYPE_VIEW_SCROLLED"
            else -> eventTypeString = event.eventType.toString()
        }
        Log.d("eventType", eventTypeString)
    }

    private fun getParent(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return

//        Log.d("TAG",  nodeInfo.className.toString() + " " + nodeInfo.text)
        this.parentElementLayout = nodeInfo
        getParent(nodeInfo.parent)
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    fun logViewHierarchy(nodeInfo: AccessibilityNodeInfo?, depth: Int) {
        if (nodeInfo == null) return
        var spacerString = ""
        for (i in 0 until depth) {
            spacerString += '-'
        }
        Log.d(
            "TAG",
            spacerString + nodeInfo.className + " " + nodeInfo.text + " " + nodeInfo.viewIdResourceName
        )
        for (i in 0 until nodeInfo.childCount) {
            logViewHierarchy(nodeInfo.getChild(i), depth + 1)
        }
    }

    private fun getListOfViewIds(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return
        if (nodeInfo.viewIdResourceName != null) {
            this.listOfViewIds.add(nodeInfo.viewIdResourceName.split("/")[1])
            if (nodeInfo.text != null)
                this.mapOfViewIdsWithText.put(
                    nodeInfo.viewIdResourceName.split("/")[1],
                    nodeInfo.text.toString()
                )
        }
        for (i in 0 until nodeInfo.childCount) {
            getListOfViewIds(nodeInfo.getChild(i))
        }

    }

    private fun initializeViewIdsList() {
        this.listOfViewIds = ArrayList();
        this.mapOfViewIdsWithText = HashMap<String, String>()
    }

    override fun onInterrupt() {}

}
