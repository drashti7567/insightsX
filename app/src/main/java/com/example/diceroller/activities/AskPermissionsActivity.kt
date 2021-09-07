package com.example.diceroller.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.diceroller.R
import com.example.diceroller.utils.PermissionsUtil


class AskPermissionsActivity : BaseActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ask_permissions)
        this.setupUI(findViewById(R.id.parent))
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun markPermissionCheckBoxes() {

        val usageStatsAccess = PermissionsUtil.checkIfAppHasUsageAccessPermission(this)
        val accessibilityServiceOn =
            PermissionsUtil.isAccessibilityServiceEnabled(this, "MyAccessibilityService")
        val isIgnoringBatteryOptimization = PermissionsUtil.checkIsIgnoringBatteryOptimizations(this)

        if(usageStatsAccess && accessibilityServiceOn && isIgnoringBatteryOptimization) {
            val trackingAppDataActivity: Intent = Intent(this, TrackingAppDataActivity::class.java)
            startActivity(trackingAppDataActivity)
            this.finish()
        }

        this.showDialogs(
            findViewById(R.id.accessibility_service_checkbox), "Accessibility Service Access",
            "Please turn InsightsX Accessibility Service On",
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            accessibilityServiceOn)

        this.showDialogs(
            findViewById(R.id.battery_optimization_checkbox), "Ignore Battery Optimization",
            "Please Ignore battery optimization for InsightsX app",
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            isIgnoringBatteryOptimization)

        this.showDialogs(
            findViewById(R.id.usage_stats_checkbox), "Usage Stats Access",
            "Please give Usage Stats Access to InsightsX Access",
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
            usageStatsAccess)

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showDialogs(
        checkbox: CheckBox, title: String, message: String,
        intent: Intent, checkboxChecked: Boolean) {
        checkbox?.isEnabled = false
        checkbox?.isChecked = checkboxChecked
        val context = this

        if (!checkbox?.isChecked)
            this.showCustomPopupMenu(
                title,
                message,
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showCustomPopupMenu(title: String, message: String, handler: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes, handler)
            .setNegativeButton(android.R.string.no) { dialog, which ->
                this.markPermissionCheckBoxes()
            }.show()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        this.markPermissionCheckBoxes()
    }
}