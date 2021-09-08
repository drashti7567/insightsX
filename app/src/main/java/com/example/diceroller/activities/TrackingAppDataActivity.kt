package com.example.diceroller.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.diceroller.R
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.IntentUtils

class TrackingAppDataActivity: BaseActivity(), View.OnClickListener {

    private var usagePattern: Button? = null
    private var systemLogs: Button? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track_app_data)

        usagePattern = findViewById<View>(R.id.usagePatternButton) as Button
        systemLogs = findViewById<View>(R.id.systemLogsButton) as Button

        usagePattern!!.setOnClickListener(this)
        systemLogs!!.setOnClickListener(this)

    }

    override fun onClick(view: View) {

        if (view === usagePattern) {
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
}