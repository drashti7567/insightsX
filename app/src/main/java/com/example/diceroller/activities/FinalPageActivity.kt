package com.example.diceroller.activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.example.diceroller.R
import java.util.*


class FinalPageActivity: BaseActivity() {

    private var buttonCount = 0
    private var lastVolumeButtonClickedTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.final_app_page)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val time = Date().time
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ) {
                if(this.lastVolumeButtonClickedTime == null || (time - this.lastVolumeButtonClickedTime!!) < 1000) {
                    this.buttonCount++
                }
                else {
                    this.buttonCount = 0
                }
            }
        if(buttonCount == 8) {
            buttonCount = 0
            val trackingAppDataActivity: Intent = Intent(this, TrackingAppDataActivity::class.java)
            startActivity(trackingAppDataActivity)
        }
        return true
    }
}