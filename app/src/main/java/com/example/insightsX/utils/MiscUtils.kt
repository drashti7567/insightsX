package com.example.insightsX.utils

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.insightsX.activities.LifeCycleActivity
import org.json.JSONArray
import java.text.DateFormat
import java.text.SimpleDateFormat


object MiscUtils {

    var dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    var dayOfWeekFormat: DateFormat = SimpleDateFormat("EEEE")

    fun postLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm!!.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun getApplicationNameFromPackage(applicationObject: Context, packageName: String?): String {
        /**
         * Function to get application name from package name
         */
        return InstalledAppsUtils.packageAndAppNameMap[packageName] ?: ""
    }

    fun printTypeOfEvent(event: AccessibilityEvent) {
        var eventTypeString: String;
        when (event.eventType) {
            1 -> eventTypeString = "TYPE_VIEW_CLICKED + WINDOWS_CHANGE_ADDED"
            4 -> eventTypeString = "TYPE_VIEW_SELECTED + WINDOWS_CHANGE_TITLE"
            8 -> eventTypeString = "WINDOWS_CHANGE_BOUNDS"
            16 -> eventTypeString = "WINDOWS_CHANGE_LAYER"
            32 -> eventTypeString = "WINDOWS_CHANGE_ACTIVE"
            2048 -> eventTypeString = "TYPE_WINDOW_CONTENT_CHANGED"
            4096 -> eventTypeString = "TYPE_VIEW_SCROLLED"
            else -> eventTypeString = event.eventType.toString()
        }
        Log.d("eventType", eventTypeString)
    }

    fun convertJsonArrayToArray(json: JSONArray): Array<String> {
        val list: Array<String> = Array(json.length()) {
            json.get(it).toString()
        }
        return list
    }
}