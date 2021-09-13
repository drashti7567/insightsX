package com.example.insightsX.tracker

import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import com.example.insightsX.constants.ApiUrlConstants
import com.example.insightsX.detectors.AppChecker
import com.example.insightsX.constants.AppNameConstants
import com.example.insightsX.database.AppDataDBHandler
import com.example.insightsX.models.AppUsageQueueData
import com.example.insightsX.utils.HttpUtils
import com.example.insightsX.utils.MiscUtils
import com.example.insightsX.utils.SharedPreferencesUtils
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList

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
                this.appUsageQueue.last().appPackageName, context)
        }
    }

    private fun writeAppSpecificQueueDataToFileOnAppEnd(appPackageName: String, context: Context) {
        if (appPackageName.equals(AppNameConstants.YOUTUBE_PACKAGE_NAME, true)) {
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
        val appUsageList: ArrayList<AppUsageQueueData> = ArrayList()
        while (this.appUsageQueue.size > 0) {
            val appUsageElement: AppUsageQueueData = this.appUsageQueue.first()
            if (appUsageElement.endTime != null || isCalledFromDestroy) {

                if (isCalledFromDestroy) appUsageElement.endTime = MiscUtils.dateFormat.format(Date())
                appUsageList.add(appUsageElement)
                this.appUsageQueue.removeFirst()
            }
            else {
                break
            }
        }
        val dbHandler = AppDataDBHandler(context).getInstance(context)
        dbHandler!!.addMultipleAppData(appUsageList)

        this.sendAppDataToServer(context)
    }

    private fun sendAppDataToServer(context: Context) {

        val appTrackerContext = this;

        val dbHelper = AppDataDBHandler(context).getInstance(context)
        val appDataList: ArrayList<AppUsageQueueData> = dbHelper!!.viewAppData()

        if(appDataList.size > 50) {

            val entity: StringEntity = appTrackerContext.createPostRequestBody(context, appDataList)

            HttpUtils.post(context, ApiUrlConstants.addAppDataToServer, entity, "application/json",
                object : AsyncHttpResponseHandler(true) {

                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        try {
                            val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                            if (serverResp.get("success") == true) {
                                dbHelper.deleteMultipleAppData(appDataList)
                            }
                        }
                        catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseBody: ByteArray?,
                        error: Throwable?) {
                        Log.d("Error", error.toString() + " " + responseBody.toString())
                    }
                })
        }
    }

    private fun createPostRequestBody(context: Context,
                                      appDataList: ArrayList<AppUsageQueueData>): StringEntity {
        val requestObj = JSONObject()
        requestObj.put("memberId", SharedPreferencesUtils.getMemberId(context))

        val usageDataArray = JSONArray()

        appDataList.forEach { data ->
            val usageObj = JSONObject()
            usageObj.put("appName", data.appName)
            usageObj.put("packageName", data.appPackageName)
            usageObj.put("day", data.dayOfWeek)
            usageObj.put("endTime", data.endTime)
            usageObj.put("startTime", data.startTime)
            usageDataArray.put(usageObj)
        }
        requestObj.put("usageData", usageDataArray)

        return StringEntity(requestObj.toString(), "UTF-8")
    }

    fun onDestroy(context: Context) {
        this.writeAppUsageToFileJob.cancel()
        this.appTrackingCoroutineJob.cancel()
        this.writeAppUsageDataToFile(context, true)
    }
}