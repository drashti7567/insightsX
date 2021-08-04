package com.example.diceroller.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Process
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager


object PermissionsUtil {

    fun checkPermissions(context: Context) {

        val isUsageStatsEnabled: Boolean = PermissionsUtil.checkIfAppHasUsageAccessPermission(context)
        if(!isUsageStatsEnabled) {
            val intent: Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent);
            return
        }

        val isAccessibilityServiceOn: Boolean =
            PermissionsUtil.isAccessibilityServiceEnabled(context,  "MyAccessibilityService")
        if(!isAccessibilityServiceOn) {
            val intent: Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        }

    }

    private fun checkIfAppHasUsageAccessPermission(context: Context): Boolean {
        val appOps =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityServiceEnabled(
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
}