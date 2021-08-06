package com.example.diceroller

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.diceroller.activities.LifeCycleActivity
import com.example.diceroller.tracker.AppTracker
import com.example.diceroller.tracker.YoutubeTracker

class MyAccessibilityService : AccessibilityService() {


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "Service Connected")

        // TODO: Empty file contents when start server integration

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
        YoutubeTracker.onDestroy(this)
        AppTracker.onDestroy(this)

        // TODO: call server to notify that accessibility service has been turned off
    }

}
