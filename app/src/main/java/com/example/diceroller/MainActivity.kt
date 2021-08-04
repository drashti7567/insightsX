package com.example.diceroller;

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.utils.IntentUtils
import com.example.diceroller.utils.PermissionsUtil


class MainActivity : AppCompatActivity(), View.OnClickListener {

    // declaring objects of Button class
    private var start: Button? = null
    private var stop: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionsUtil.checkPermissions(this)

//        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        start = findViewById<View>(R.id.startButton) as Button
        stop = findViewById<View>(R.id.stopButton) as Button

        start!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        if (view === start) {
            IntentUtils.createIntentTOShareCsv(
                this, FileNameConstants.APP_USAGE_FILE_NAME, "Share App Usage Data")
        }
    }


}
