package com.example.diceroller.tracker

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.diceroller.AppChecker
import com.example.diceroller.constants.AppNameConstants
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.models.AppUsageQueueData
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.MiscUtils
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        if (appName.equals(AppNameConstants.YOUTUBE_NAME, true)) {
            YoutubeTracker.writeYoutubeUsageDataToFile(context)
        }
    }

    private fun startJobToWriteToFile(context: Context) {
        val appTrackerContext = this
        this.writeAppUsageToFileJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                appTrackerContext.writeAppUsageDataToFile(context)
                delay(10000)
            }
        }
    }

    private fun writeAppUsageDataToFile(context: Context) {
        var csvChunk: String = ""
        while (this.appUsageQueue.size > 0) {
            val appUsageElement: AppUsageQueueData = this.appUsageQueue.first()
            if (appUsageElement.endTime != null) {
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
            csvChunk
        )

    }
}