package com.example.diceroller

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.diceroller.activities.LifeCycleActivity
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.tracker.AppTracker
import com.example.diceroller.tracker.YoutubeTracker
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.MiscUtils
import java.util.*

class MyAccessibilityService : AccessibilityService() {


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "Service Connected")

        // TODO: Empty file contents when start server integration

        FileUtils.writeFileOnInternalStorage(
            this, FileNameConstants.SYSTEM_LOGS_FILE_NAME,
            MiscUtils.dateFormat.format(Date()) + ", Accessibility Service On\n",
            FileNameConstants.SYSTEM_LOGS_FILE_HEADERS)
        AppTracker.startAppTracker(this)
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

    override fun onDestroy() {
        super.onDestroy()
        FileUtils.writeFileOnInternalStorage(
            this, FileNameConstants.SYSTEM_LOGS_FILE_NAME,
            MiscUtils.dateFormat.format(Date()) + ",Accessibility Service OFF \n",
            FileNameConstants.SYSTEM_LOGS_FILE_HEADERS)
        YoutubeTracker.onDestroy(this)
        AppTracker.onDestroy(this)

        // TODO: call server to notify that accessibility service has been turned off
    }

}
