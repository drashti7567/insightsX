package com.example.insightsX.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.services.AppTrackerService

class RestartAppTrackerIfAppClosed: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION) {
            Intent(context, AppTrackerService::class.java).also {
                it.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it)
                    return
                }
                context.startService(it)
            }
        }
    }
}