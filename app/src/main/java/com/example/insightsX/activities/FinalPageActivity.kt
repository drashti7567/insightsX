package com.example.insightsX.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import com.example.insightsX.R
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.services.AppTrackerService
import java.util.*


class FinalPageActivity : BaseActivity() {

    private var buttonCount = 0
    private var lastVolumeButtonClickedTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.final_app_page)

        this.startAppTrackerService()
    }

    private fun startAppTrackerService() {
        /**
         * Function to start AppTracker service as foreground service.
         */
        val startServiceIntent: Intent = Intent(this, AppTrackerService::class.java)
        startServiceIntent.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startServiceIntent)
            return
        } else {
            startService(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        /**
         * Shortcut function which on click on volume down button 8 times will show track activity page
         */
        val time = Date().time
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (this.lastVolumeButtonClickedTime == null || (time - this.lastVolumeButtonClickedTime!!) < 1000) {
                this.buttonCount++
            }
            else {
                this.buttonCount = 0
            }
        }
        if (buttonCount == 8) {
            buttonCount = 0
            val trackingAppDataActivity: Intent = Intent(this, TrackingAppDataActivity::class.java)
            startActivity(trackingAppDataActivity)
        }

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            super.onBackPressed()
        }
        return true;
    }
}