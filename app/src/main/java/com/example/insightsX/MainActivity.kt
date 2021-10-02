package com.example.insightsX;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.insightsX.activities.EntryPoint
import com.example.insightsX.constants.FileNameConstants
import com.example.insightsX.utils.FileUtils
import com.example.insightsX.utils.MiscUtils
import com.example.insightsX.utils.PowerManagerUtils
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {

            Handler(Looper.getMainLooper()).postDelayed({
                val startTrackingAppDataActivity: Intent =
                    Intent(this.applicationContext, EntryPoint::class.java)
                startActivity(startTrackingAppDataActivity)
                this.finish()
            }, 1000)

            this.registerReceiverForPowerSaver(this)
            this.printLauncherName()
        }
        catch(e: Exception) {
            Log.d("Insta Tracker", e.stackTrace.toString())
        }
    }

    private fun printLauncherName() {
        val localPackageManager = packageManager
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        val str = localPackageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY)!!.activityInfo.packageName
        Log.d("Package Name:", str)
    }


    private fun registerReceiverForPowerSaver(mainActivity: MainActivity) {
        val powerSaverChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onReceive(context: Context?, intent: Intent?) {
                val systemLogDesc: String =
                    if (PowerManagerUtils.checkIfPowerSaverIsOn(mainActivity)) ", Power Saver ON" else ", Power Saver OFF"
                FileUtils.writeFileOnInternalStorage(
                    mainActivity, FileNameConstants.SYSTEM_LOGS_FILE_NAME,
                    MiscUtils.dateFormat.format(Date()) + systemLogDesc + "\n",
                    FileNameConstants.SYSTEM_LOGS_FILE_HEADERS
                )
            }
        }

        val filter = IntentFilter()
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED")
        registerReceiver(powerSaverChangeReceiver, filter)
    }
}
