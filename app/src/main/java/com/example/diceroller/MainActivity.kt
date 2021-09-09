package com.example.diceroller;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroller.activities.EnterMemberIdActivity
import com.example.diceroller.activities.EntryPoint
import com.example.diceroller.activities.SystemLogsActivity
import com.example.diceroller.activities.TrackingAppDataActivity
import com.example.diceroller.activities.UsagePatternActivity
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.IntentUtils
import com.example.diceroller.utils.MiscUtils
import com.example.diceroller.utils.PermissionsUtil
import com.example.diceroller.utils.PowerManagerUtils
import java.util.*


class MainActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            val startTrackingAppDataActivity: Intent =
                Intent(this.applicationContext, EntryPoint::class.java)
            startActivity(startTrackingAppDataActivity)
            this.finish()
        }, 1000)

        this.registerReceiverForPowerSaver(this)
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
