package com.example.diceroller.tracker

import android.app.KeyguardManager
import android.content.Context
import com.example.diceroller.detectors.AppChecker
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

    fun startAppTracker(context: Context) {
        this.appUsageQueue = ArrayDeque();
        this.startJobForCheckingAppUpdates(context)
        this.startJobToWriteToFile(context)
    }

    private fun startJobForCheckingAppUpdates(context: Context) {
        val appTrackerContext: AppTracker = this
        this.appTrackingCoroutineJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val currentProcessPackageName = AppChecker.getForegroundApp(context)
                val currentApplicationName =
                    MiscUtils.getApplicationNameFromPackage(context, currentProcessPackageName)

                val currentDate = Date()

                val myKM: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager;

                if (myKM.isKeyguardLocked) {
                    appTrackerContext.recordEndTimeOnDisruption(context, currentDate)
                }
                else {
                    // Discard if its quickstart launcher and place end time of last app.
                    if (currentApplicationName.equals(AppNameConstants.LAUNCHER_NAME, true) ||
                        currentApplicationName.equals(AppNameConstants.ANDROID_SYSTEM, true)) {
                        appTrackerContext.recordEndTimeOnDisruption(context, currentDate)
                    }
                    else {
                        // Check if application has switched and last applications end time is yet to be recorderd
                        if (appTrackerContext.appUsageQueue.size > 0 &&
                            appTrackerContext.appUsageQueue.last().endTime == null &&
                            !currentApplicationName.equals(appTrackerContext.appUsageQueue.last().appName, true)) {
                            appTrackerContext.recordEndTimeOnDisruption(context, currentDate)
                        }
                        // If last application has ended, then push this app in queue
                        if (appTrackerContext.appUsageQueue.size == 0 ||
                            (appTrackerContext.appUsageQueue.size > 0 &&
                                    appTrackerContext.appUsageQueue.last().endTime != null)) {
                            val appUsageInfo: AppUsageQueueData =
                                AppUsageQueueData(
                                    currentProcessPackageName, currentApplicationName,
                                    MiscUtils.dayOfWeekFormat.format(currentDate),
                                    MiscUtils.dateFormat.format(currentDate), null
                                )

                            appTrackerContext.appUsageQueue.addLast(appUsageInfo)
                        }
                    }
                }

                delay(1000)
            }
        }
    }

    private fun recordEndTimeOnDisruption(context: Context, currentDate: Date) {
        if (this.appUsageQueue.size > 0 &&
            this.appUsageQueue.last().endTime == null) {
            this.appUsageQueue.last().endTime =
                MiscUtils.dateFormat.format(currentDate)
            this.writeAppSpecificQueueDataToFileOnAppEnd(
                this.appUsageQueue.last().appName, context)
        }
    }

    private fun writeAppSpecificQueueDataToFileOnAppEnd(appName: String, context: Context) {
        if (appName.equals(AppNameConstants.YOUTUBE_PACKAGE_NAME, true)) {
            YoutubeTracker.writeYoutubeUsageDataToFile(context)
        }
    }

    private fun startJobToWriteToFile(context: Context) {
        val appTrackerContext = this
        this.writeAppUsageToFileJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                appTrackerContext.writeAppUsageDataToFile(context, false)
                delay(10000)
            }
        }
    }

    private fun writeAppUsageDataToFile(context: Context, isCalledFromDestroy: Boolean = false) {
        var csvChunk: String = ""
        while (this.appUsageQueue.size > 0) {
            val appUsageElement: AppUsageQueueData = this.appUsageQueue.first()
            if (appUsageElement.endTime != null || isCalledFromDestroy) {

                if (isCalledFromDestroy) appUsageElement.endTime = MiscUtils.dateFormat.format(Date())
                val csvRow: String =
                    appUsageElement.appName + "," + appUsageElement.appPackageName + "," + appUsageElement.dayofWeek +
                            "," + appUsageElement.startTime + "," + appUsageElement.endTime + "\n"
                csvChunk += csvRow
                this.appUsageQueue.removeFirst()
            }
            else {
                break
            }
        }
        FileUtils.writeFileOnInternalStorage(
            context,
            FileNameConstants.APP_USAGE_FILE_NAME,
            csvChunk,
            FileNameConstants.APP_USAGE_FILE_HEADERS
        )
    }

    fun onDestroy(context: Context) {
        this.writeAppUsageToFileJob.cancel()
        this.appTrackingCoroutineJob.cancel()
        this.writeAppUsageDataToFile(context, true)
    }
}