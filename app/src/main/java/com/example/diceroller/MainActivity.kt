package com.example.diceroller;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroller.activities.SystemLogsActivity
import com.example.diceroller.activities.UsagePatternActivity
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.IntentUtils
import com.example.diceroller.utils.MiscUtils
import com.example.diceroller.utils.PermissionsUtil
import com.example.diceroller.utils.PowerManagerUtils
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    // declaring objects of Button class
    private var start: Button? = null
    private var stop: Button? = null

    private var usagePattern: Button? = null
    private var systemLogs: Button? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionsUtil.checkPermissions(this)

        start = findViewById<View>(R.id.startButton) as Button
        stop = findViewById<View>(R.id.stopButton) as Button
        usagePattern = findViewById<View>(R.id.usagePatternButton) as Button
        systemLogs = findViewById<View>(R.id.systemLogsButton) as Button

        start!!.setOnClickListener(this)
        stop!!.setOnClickListener(this)
        usagePattern!!.setOnClickListener(this)
        systemLogs!!.setOnClickListener(this)

        this.registerReceiverForPowerSaver(this)
    }

    override fun onClick(view: View) {

        if (view === start) {
            try {
                IntentUtils.createIntentTOShareCsv(
                    this, FileNameConstants.APP_USAGE_FILE_NAME, "Share App Usage Data", "App Usage Data")
            }
            catch (e: Exception) {
                Toast.makeText(this, "Apps not browsed yet.", Toast.LENGTH_SHORT).show()
            }
        }
        else if (view === stop) {
            try {
                Log.d(
                    "File Content",
                    FileUtils.readFileOnInternalStorage(this, FileNameConstants.YOUTUBE_USAGE_FILE_NAME))
                IntentUtils.createIntentTOShareCsv(
                    this, FileNameConstants.YOUTUBE_USAGE_FILE_NAME, "Share Youtube Usage Data", "Youtube Usage Data")
            }
            catch (e: Exception) {
                Toast.makeText(this, "Youtube not browsed yet. Please open youtube.", Toast.LENGTH_SHORT).show()
            }
        }
        else if (view === usagePattern) {
            Log.d("TAG", "Usage Pattern Clicked!!")
            val startUsagePatternActivity: Intent = Intent(this.applicationContext, UsagePatternActivity::class.java)
            startActivity(startUsagePatternActivity)
        }
        else if (view === systemLogs) {
            Log.d("TAG", "System Logs Clicked!!")
            val startSystemLogsActivity: Intent = Intent(this.applicationContext, SystemLogsActivity::class.java)
            startActivity(startSystemLogsActivity)
        }

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
