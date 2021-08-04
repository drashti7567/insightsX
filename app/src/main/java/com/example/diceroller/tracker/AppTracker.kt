package com.example.diceroller.tracker

import android.content.Context
import android.util.Log
import com.example.diceroller.AppChecker
import com.example.diceroller.constants.AppNameConstants
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.models.AppUsageQueueData
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.MiscUtils
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayDeque

object AppTracker {

    lateinit var appUsageQueue: ArrayDeque<AppUsageQueueData>

    lateinit var appTrackingCoroutineJob: Job

    lateinit var writeAppUsageToFileJob: Job

    fun startAppTracker(appChecker: AppChecker, context: Context) {
        this.appUsageQueue = ArrayDeque();
        this.startJobForCheckingAppUpdates(appChecker, context)
        this.startJobToWriteToFile(context)
    }

    private fun startJobForCheckingAppUpdates(appChecker: AppChecker, context: Context) {
        val appTrackerContext: AppTracker = this
        this.appTrackingCoroutineJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val currentProcessPackageName = appChecker.getForegroundApp(context)
                val currentApplicationName =
                    MiscUtils.getApplicationNameFromPackage(context, currentProcessPackageName)

                val currentDate: String = Date().toString()

                if (appTrackerContext.appUsageQueue.size > 0 &&
                        appTrackerContext.appUsageQueue.last().endTime == null)
                    appTrackerContext.appUsageQueue.last().endTime = currentDate

                if(!currentApplicationName.equals(AppNameConstants.LAUNCHER_NAME, true)) {
                    val appUsageInfo: AppUsageQueueData =
                        AppUsageQueueData(
                            currentProcessPackageName, currentApplicationName,
                            currentDate, null
                        )

                    appTrackerContext.appUsageQueue.addLast(appUsageInfo)
                }

                delay(2000)
            }
        }
    }

    private fun startJobToWriteToFile(context: Context) {
        val appTrackerContext = this
        this.writeAppUsageToFileJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                appTrackerContext.writeAppUsageDataToFile(context)
                delay(6000)
            }
        }
    }

    private fun writeAppUsageDataToFile(context: Context) {
        var csvChunk: String = ""
        while (this.appUsageQueue.size > 0) {
            val appUsageElement: AppUsageQueueData = this.appUsageQueue.first()
            if (appUsageElement.endTime != null) {
                val csvRow: String =
                    appUsageElement.appName + "," + appUsageElement.appPackageName +
                            "," + appUsageElement.startTime + "," + appUsageElement.endTime + "\n"
                csvChunk += csvRow
                this.appUsageQueue.removeFirst()
            } else {
                break
            }
        }
        FileUtils.writeFileOnInternalStorage(
            context,
            FileNameConstants.APP_USAGE_FILE_NAME,
            csvChunk
        )

        Log.d(
            "FILE_CONTENT",
            FileUtils.readFileOnInternalStorage(
                context,
                FileNameConstants.APP_USAGE_FILE_NAME
            )
        )

    }
}