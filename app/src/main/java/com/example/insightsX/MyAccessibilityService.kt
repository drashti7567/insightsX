package com.example.insightsX

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.insightsX.activities.LifeCycleActivity
import com.example.insightsX.constants.AppPackageNameConstants
import com.example.insightsX.constants.FileNameConstants
import com.example.insightsX.tracker.AppTracker
import com.example.insightsX.tracker.InstagramTracker
import com.example.insightsX.tracker.YoutubeTracker
import com.example.insightsX.utils.FileUtils
import com.example.insightsX.utils.MiscUtils
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

        if (event.eventType == 4096) return
        if (event.eventType === AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            !LifeCycleActivity.allowWindowContentChangeEvent) return
        if (event.eventType == AccessibilityEvent.TYPE_ANNOUNCEMENT) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            LifeCycleActivity.allowWindowContentChangeEvent = true
        }

        filterBasedOnAppPackageName(event)

    }

    private fun filterBasedOnAppPackageName(event: AccessibilityEvent) {

        if (event.packageName != null && event.packageName.equals(AppPackageNameConstants.youtubePackage)) {
            if(event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
                return
            }
            YoutubeTracker.onYoutubeEventReceived(event, this)
        };

        if (event.packageName != null && event.packageName.equals(AppPackageNameConstants.instagramPackage)) {
            InstagramTracker.onEventReceived(event, this)
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
