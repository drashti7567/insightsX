package com.example.insightsX.activities

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle

class LifeCycleActivity : Application(), ActivityLifecycleCallbacks {

    companion object {
        var allowWindowContentChangeEvent: Boolean = true
        var context: Context? = null
    }

    var activityReferences: Int = 0
    var isActivityChangingConfigurations: Boolean = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        context = applicationContext
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
//            Log.d("TAG", "Foreground")
//            PermissionsUtil.checkPermissions(this)

        }
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
        }
    }
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}