package com.example.insightsX.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.services.AppTrackerService
import com.example.insightsX.utils.ScheduleStartAppTrackerAppUtil

class RestartAppTrackerIfPhoneRestarted: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        /**
         * onReceive is called when a broadcast is received.
         */
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ScheduleStartAppTrackerAppUtil.startPeriodicWork(context)
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