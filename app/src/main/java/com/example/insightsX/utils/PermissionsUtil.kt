package com.example.insightsX.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi


object PermissionsUtil {

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissions(context: Context) {

        val isUsageStatsEnabled: Boolean = checkIfAppHasUsageAccessPermission(context)
        if(!isUsageStatsEnabled) {
            val intent: Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent);
            return
        }

        val isIgnoringBatteryOptimization: Boolean = checkIsIgnoringBatteryOptimizations(context)
        if(!isIgnoringBatteryOptimization) {
            val intent: Intent = Intent()
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
//            intent.data = Uri.parse("package: " + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }

        val isAccessibilityServiceOn: Boolean =
            isAccessibilityServiceEnabled(context,  "MyAccessibilityService")
        if(!isAccessibilityServiceOn) {
            val intent: Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }

    }

    fun checkIfAppHasUsageAccessPermission(context: Context): Boolean {
        val appOps =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceName: String
    ): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (enabledService in enabledServices) {
            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName.equals(context.packageName) && enabledServiceInfo.name.equals(
                    context.packageName + "." + serviceName
                )
            ) return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkIsIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}