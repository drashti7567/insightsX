package com.example.insightsX.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import com.example.insightsX.R
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.services.AppTrackerService
import com.example.insightsX.utils.ScheduleStartAppTrackerAppUtil
import java.util.*


class FinalPageActivity : BaseActivity() {

    private var buttonCount = 0
    private var lastVolumeButtonClickedTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.final_app_page)
        ScheduleStartAppTrackerAppUtil.startPeriodicWork(applicationContext)
        this.startAppTrackerService()
        this.registerReceiverForAudioPlayback()
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

    private fun registerReceiverForAudioPlayback() {
        val iF = IntentFilter()
        iF.addAction("com.android.music.metachanged")
        iF.addAction("com.android.music.playstatechanged")
        iF.addAction("com.android.music.playbackcomplete")
        iF.addAction("com.android.music.queuechanged")
        iF.addAction("com.htc.music.metachanged")
        iF.addAction("fm.last.android.metachanged")
        iF.addAction("com.sec.android.app.music.metachanged")
        iF.addAction("com.nullsoft.winamp.metachanged")
        iF.addAction("com.amazon.mp3.metachanged")
        iF.addAction("com.miui.player.metachanged")
        iF.addAction("com.real.IMP.metachanged")
        iF.addAction("com.sonyericsson.music.metachanged")
        iF.addAction("com.rdio.android.metachanged")
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged")
        iF.addAction("com.andrew.apollo.playbackstatechanged")
        iF.addAction("com.spotify.music.metadatachanged")
        iF.addAction("com.spotify.music.metachanged")
        iF.addAction("com.spotify.music.queuechanged")
        iF.addAction("com.spotify.mobile.android.playbackstatechanged")
        iF.addAction("com.spotify.music.metadatachanged")
        iF.addAction("com.spotify.music.queuechanged")
        iF.addAction("com.real.IMP.metachanged")
        registerReceiver(mReceiver, iF)
    }

    fun query(
        context: Context,
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?,
        limit: Int): Cursor? {
        var uri: Uri = uri
        return try {
            val resolver = context.contentResolver ?: return null
            if (limit > 0) uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build()
            resolver.query(uri, projection, selection, selectionArgs, sortOrder)
        }
        catch (ex: UnsupportedOperationException) {
            null
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            val path = intent.getStringExtra("track")!!.replace("'".toRegex(), "''")
            Log.d("RECEIVER", intent.getStringExtra("track").toString())
//            val c: Cursor? = query(
//                context,
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                arrayOf(MediaStore.Audio.Media.DATA),
//                MediaStore.Audio.Media.TITLE + "='" + path + "'",
//                null,
//                null,
//                0)
//            try {
//                if (c == null || c.getCount() === 0) return
//                val size: Int = c.getCount()
//                if (size != 1) return
//                c.moveToNext()
//
//                // Here's the song path
//                val songPath: String = c.getString(0)
//                Log.d("ROAST_APP", "" + songPath)
//            }
//            finally {
//                if (c != null) c.close()
//            }
        }
    }
}