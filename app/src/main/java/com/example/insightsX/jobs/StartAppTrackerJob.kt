package com.example.insightsX.jobs

//import android.annotation.SuppressLint
//import android.content.Intent
//import android.util.Log
//import com.example.insightsX.constants.ForegroundServiceConstants
//import com.example.insightsX.services.AppTrackerService
//import com.firebase.jobdispatcher.JobParameters
//import com.firebase.jobdispatcher.JobService
//
//@SuppressLint("NewApi")
//class StartAppTrackerJob: JobService() {
//
//    override fun onStartJob(p0: JobParameters?): Boolean {
//        Log.d("JOB", "-----------------------------------------")
//        Log.d("JOb", "Start App Tracker job started")
//        Log.d("JOB", "-----------------------------------------")
//        val broadcastIntent: Intent = Intent(this, AppTrackerService::class.java)
//        broadcastIntent.action = ForegroundServiceConstants.ACTION.STARTFOREGROUND_ACTION
//        sendBroadcast(broadcastIntent)
//        return false
//    }
//
//    override fun onStopJob(p0: JobParameters?): Boolean {
//        return false
//    }
//}