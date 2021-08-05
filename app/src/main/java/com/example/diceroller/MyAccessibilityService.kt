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
import com.example.diceroller.activities.LifeCycleActivity
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.constants.YoutubeViewIdConstants
import com.example.diceroller.tracker.AppTracker
import com.example.diceroller.tracker.YoutubeTracker
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.NodeInfoUtils
import kotlinx.coroutines.*
import java.io.File

class MyAccessibilityService : AccessibilityService() {


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "Service Connected")

        val appChecker: AppChecker = AppChecker();
        FileUtils.emptyFileContents(
            this,
            FileNameConstants.APP_USAGE_FILE_NAME,
            FileNameConstants.APP_USAGE_FILE_HEADERS)
        FileUtils.emptyFileContents(
            this,
            FileNameConstants.YOUTUBE_USAGE_FILE_NAME,
            FileNameConstants.YOUTUBE_USAGE_FILE_HEADERS)
        AppTracker.startAppTracker(appChecker, this)
    }


    @SuppressLint("NewApi")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (event.eventType === AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            !LifeCycleActivity.allowWindowContentChangeEvent) return
        if (event.eventType == 4096) return
        if (event.eventType == AccessibilityEvent.TYPE_ANNOUNCEMENT) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            LifeCycleActivity.allowWindowContentChangeEvent = true
        }

        filterBasedOnAppPackageName(event)

    }

    private fun filterBasedOnAppPackageName(event: AccessibilityEvent) {
        if (event.packageName != null && event.packageName.equals("com.google.android.youtube")) {
            YoutubeTracker.onYoutubeEventReceived(event, this)
        };
    }

    override fun onInterrupt() {}

}
