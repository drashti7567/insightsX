package com.example.diceroller;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroller.activities.UsagePatternActivity
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.IntentUtils
import com.example.diceroller.utils.PermissionsUtil


class MainActivity : AppCompatActivity(), View.OnClickListener {

    // declaring objects of Button class
    private var start: Button? = null
    private var stop: Button? = null

    private var usagePattern: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionsUtil.checkPermissions(this)

//        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        start = findViewById<View>(R.id.startButton) as Button
        stop = findViewById<View>(R.id.stopButton) as Button
        usagePattern = findViewById<View>(R.id.usagePatternButton) as Button

        start!!.setOnClickListener(this)
        stop!!.setOnClickListener(this)
        usagePattern!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        if (view === start) {
            IntentUtils.createIntentTOShareCsv(
                this, FileNameConstants.APP_USAGE_FILE_NAME, "Share App Usage Data", "App Usage Data")
        }
        else if(view === stop) {
            Log.d("File Content", FileUtils.readFileOnInternalStorage(this, FileNameConstants.YOUTUBE_USAGE_FILE_NAME))
            IntentUtils.createIntentTOShareCsv(
                this, FileNameConstants.YOUTUBE_USAGE_FILE_NAME, "Share Youtube Usage Data", "Youtube Usage Data")
        }
        else if(view === usagePattern) {
            Log.d("TAG", "Usage Pattern Clicked!!")
            val startUsagePatternActivity: Intent = Intent(this.applicationContext, UsagePatternActivity::class.java)
            startActivity(startUsagePatternActivity)
        }
    }


}
