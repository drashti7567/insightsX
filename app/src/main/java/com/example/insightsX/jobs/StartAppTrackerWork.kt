package com.example.insightsX.jobs;

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.services.AppTrackerService

class StartAppTrackerWork(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("JOB", "-----------------------------------------")
        Log.d("JOb", "Start App Tracker job started")
        Log.d("JOB", "-----------------------------------------")
        Intent(context, AppTrackerService::class.java).also {
            it.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(it)
                return Result.success()
            }
            context.startService(it)
        }
        return Result.success()
    }
}
