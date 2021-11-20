package com.example.insightsX.activities

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.services.AppTrackerService

class LifeCycleActivity : MultiDexApplication(), LifecycleObserver {

    companion object {
        var allowWindowContentChangeEvent: Boolean = true
        var context: Context? = null
    }

    var activityReferences: Int = 0
    var isActivityChangingConfigurations: Boolean = false

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this);
        val broadcastIntent: Intent = Intent(this, AppTrackerService::class.java)
        broadcastIntent.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION
        sendBroadcast(broadcastIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        this.sendBroadcastToStartAppTrackerService()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onAppForegrounded() {
       this.sendBroadcastToStartAppTrackerService()
    }


    private fun sendBroadcastToStartAppTrackerService() {
        /**
         * FUnction send a broadcast to start app tracker service.
         */
        val broadcastIntent: Intent = Intent(this, AppTrackerService::class.java)
        broadcastIntent.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION
        sendBroadcast(broadcastIntent)
    }

}