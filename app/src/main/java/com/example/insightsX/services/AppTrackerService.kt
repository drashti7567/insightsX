package com.example.insightsX.services

import android.R
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.speech.tts.TextToSpeech.STOPPED
import android.telephony.ServiceState
import android.util.Log
import android.widget.Toast
import com.example.insightsX.MainActivity
import com.example.insightsX.constants.ForegroundServiceConstants
import com.example.insightsX.tracker.AppTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AppTrackerService: Service() {

    companion object {
        public var isServiceStarted = false
    }

    private var wakeLock: PowerManager.WakeLock? = null


    private val LOG_TAG = "AppTrackerService"

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "The service has been created")
        var notification = createNotification()
        startForeground(ForegroundServiceConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "------------------------------------------------------")
        Log.d(LOG_TAG,"onStartCommand executed with startId: $startId")
        Log.d(LOG_TAG, "------------------------------------------------------")
        if (intent != null) {
            val action = intent.action
            Log.d(LOG_TAG, "using an intent with action $action")
            when (action) {
                ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION -> startService()
                ForegroundServiceConstants.ACTION.STOPFOREGROUND_ACTION -> stopService()
                else -> Log.d(LOG_TAG,"This should never happen. No action in the received intent")
            }
        } else {
            Log.d(LOG_TAG, "with a null intent. It has been probably restarted by the system.")
            this.startService()
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    private fun startService() {
        if (isServiceStarted) return
        Log.d(LOG_TAG, "Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
//        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
//        GlobalScope.launch(Dispatchers.IO) {
//            while (isServiceStarted) {
//                launch(Dispatchers.IO) {
//
//                }
//                delay(2000)
//            }
//            Log.d(LOG_TAG, "End of the loop for the service")
//        }
        AppTracker.startAppTracker(this)
    }

    private fun stopService() {
        Log.d(LOG_TAG, "Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
//        setServiceState(this, ServiceState.STOPPED)
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("InsightsX")
            .setContentText("You are all set!!")
            .setContentIntent(pendingIntent)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        /**
         * Function used in cased of bound services.
         * Not useful in our case
         */
        return null;
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        /**
         * Added here the code to restart the service, if the app is removed from task manager
         * by clicking on clear all apps.
         */
        val restartServiceIntent = Intent(applicationContext, AppTrackerService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "The service has been destroyed".toUpperCase())
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
        val broadcastIntent: Intent = Intent(this, AppTrackerService::class.java)
        broadcastIntent.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION
        sendBroadcast(broadcastIntent)
    }
}