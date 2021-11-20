package com.example.insightsX.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.insightsX.jobs.StartAppTrackerWork
import java.util.concurrent.TimeUnit


object ScheduleStartAppTrackerAppUtil {

    val startAppTrackerJobTag: String = "StartAppTrackerJob"
    val startAppTrackerWorkTag: String = "StartAppTrackerWork"

    fun startPeriodicWork(context: Context) {
        /**
         * Function is responsible for starting the work manager
         */
        val startAppTrackerWorkRequest =
            PeriodicWorkRequestBuilder<StartAppTrackerWork>(15, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            startAppTrackerWorkTag, ExistingPeriodicWorkPolicy.REPLACE, startAppTrackerWorkRequest)
    }

//    fun scheduleJob(context: Context) {
//        /**
//         * FUnction responsible for scheduling the firebase job dispatcher(DEPRECATED AND USE WORK MANAGER INSTEAD)
//         * to start the app tracker service.
//         */
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
//        //creating new job and adding it with dispatcher
//        //creating new job and adding it with dispatcher
//        val job: Job = createJob(dispatcher)
//        dispatcher.mustSchedule(job)
//    }

//    fun createJob(dispatcher: FirebaseJobDispatcher): Job {
//        /**
//         * Function that creates a job that calls StartAppTrackerService every half hour.
//         */
//        return dispatcher.newJobBuilder() //persist the task across boots
//            //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)
//            //call this service when the criteria are met.
//            .setService(StartAppTrackerJob::class.java)
//            .setLifetime(Lifetime.FOREVER)
//            .setTag(this.startAppTrackerJobTag)
//            .setReplaceCurrent(false)
//            .setRecurring(true)
//            .setTrigger(Trigger.executionWindow(0, 20))
//            .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
////            .setConstraints(Constraint.ON_ANY_NETWORK, Constraint.DEVICE_CHARGING)
//            .build()
//    }
//
//    fun cancelJob(context: Context?) {
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
//        //Cancel all the jobs for this package
//        dispatcher.cancelAll()
//        // Cancel the job for this tag
//        dispatcher.cancel(this.startAppTrackerJobTag)
//    }

}

