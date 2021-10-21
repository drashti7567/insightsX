package com.example.insightsX

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.insightsX.activities.LifeCycleActivity
import com.example.insightsX.constants.ApiUrlConstants
import com.example.insightsX.constants.AppPackageNameConstants
import com.example.insightsX.constants.FileNameConstants
import com.example.insightsX.models.InstalledAppsData
import com.example.insightsX.tracker.AppTracker
import com.example.insightsX.tracker.InstagramTracker
import com.example.insightsX.tracker.YoutubeTracker
import com.example.insightsX.utils.FileUtils
import com.example.insightsX.utils.HttpUtils
import com.example.insightsX.utils.InstalledAppsUtils
import com.example.insightsX.utils.MiscUtils
import com.example.insightsX.utils.SharedPreferencesUtils
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.util.*

class MyAccessibilityService : AccessibilityService() {


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TAG", "Service Connected")

        // TODO: Empty file contents when start server integration

        this.uploadInstalledApps()

        this.notifyServerThatServiceStartedOrDestroyed(true)

        FileUtils.writeFileOnInternalStorage(
            this, FileNameConstants.SYSTEM_LOGS_FILE_NAME,
            MiscUtils.dateFormat.format(Date()) + ", Accessibility Service On\n",
            FileNameConstants.SYSTEM_LOGS_FILE_HEADERS)
        AppTracker.startAppTracker(this)
    }


    @SuppressLint("NewApi")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        try {
            if (event.eventType == 4096) return
            if (event.eventType === AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
                !LifeCycleActivity.allowWindowContentChangeEvent) return
            if (event.eventType == AccessibilityEvent.TYPE_ANNOUNCEMENT) return

            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                LifeCycleActivity.allowWindowContentChangeEvent = true
            }

            filterBasedOnAppPackageName(event)
        }
        catch(e: Exception) {
            Log.d("Main Accessibility", e.stackTrace.toString())
        }

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

    private fun notifyServerThatServiceStartedOrDestroyed(serviceOn: Boolean) {
        /**
         * Call backend API that tracks whether user's service is currently on or off.
         */
        val memberId: String = SharedPreferencesUtils.getMemberId(this)!!
        val context = this;

        val url: String = if (serviceOn) "${ApiUrlConstants.serviceTurnedOn}${memberId}"
                            else "${ApiUrlConstants.serviceTurnedOff}${memberId}"

        HttpUtils.get(url, RequestParams(), object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                Log.d("Success", serverResp.toString())
            }
            override fun onFailure(statusCode: Int, headers: Array<out Header>?,
                                   responseBody: ByteArray?, error: Throwable?) {
                Log.d("Error", error.toString() + " " + responseBody.toString())
            }
        })
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            FileUtils.writeFileOnInternalStorage(
                this, FileNameConstants.SYSTEM_LOGS_FILE_NAME,
                MiscUtils.dateFormat.format(Date()) + ",Accessibility Service OFF \n",
                FileNameConstants.SYSTEM_LOGS_FILE_HEADERS)
            YoutubeTracker.onDestroy(this)
            AppTracker.onDestroy(this)
            InstagramTracker.onDestroy(this)
        }
        catch(e: Exception) {
            Log.d("Main Accessibility", e.stackTrace.toString())
        }

        this.notifyServerThatServiceStartedOrDestroyed(false)
    }

    private fun uploadInstalledApps() {
        /**
         * Function to upload the installed apps of the member's phone to server db
         */
        if(SharedPreferencesUtils.getInstalledAppsUploaded(this) != null &&
            SharedPreferencesUtils.getInstalledAppsUploaded(this) == false) {

            val installedAppsList: ArrayList<InstalledAppsData> =
                InstalledAppsUtils.getInstalledApps(this.packageManager);
            val entity: StringEntity = this.createPostRequestBody(this, installedAppsList);
            val context = this

            HttpUtils.post(this, ApiUrlConstants.addInstalledAppsToDb, entity, "application/json",
                object: AsyncHttpResponseHandler(true) {

                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        try {
                            val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                            if (serverResp.get("success") == true) {
                                SharedPreferencesUtils.setInstalledAppsUploaded(context, true);
                            }
                            else {
                                Log.d("Error", serverResp.get("message").toString())
                            }
                        }
                        catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    override fun onFailure(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?, error: Throwable?) {
                        Log.d("Error", error.toString() + " " + responseBody.toString())
                    }
                })
        }
    }

    private fun createPostRequestBody(context: Context,
                                      installedAppsList: ArrayList<InstalledAppsData>): StringEntity {
        /**
         * Function to create String entity - json object for the post request
         * to upload the installed apps list to server.
         */
        val requestObj = JSONObject()
        requestObj.put("memberId", SharedPreferencesUtils.getMemberId(context))

        val installedAppsArray = JSONArray()

        installedAppsList.forEach { data ->
            val usageObj = JSONObject()
            usageObj.put("appName", data.appName)
            usageObj.put("isSystemApp", data.isSystemPackage)
            usageObj.put("packageName", data.packageName)
            installedAppsArray.put(usageObj)
        }
        requestObj.put("installedAppsList", installedAppsArray)

        return StringEntity(requestObj.toString(), "UTF-8")
    }

}
