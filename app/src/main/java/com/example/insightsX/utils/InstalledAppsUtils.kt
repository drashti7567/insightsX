package com.example.insightsX.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.util.Log
import com.example.insightsX.models.InstalledAppsData

object InstalledAppsUtils {

    var packageAndAppNameMap = HashMap<String, String>();

    private var currentLauncherName: String? = null;

    fun getInstalledApps(packageManager: PackageManager, context: Context): ArrayList<InstalledAppsData> {
        /**
         * Function to get list of all apps that can be launched.
         */

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)

        val installedPackagesList = ArrayList<InstalledAppsData>()
        pkgAppsList.forEach {
            if (it.activityInfo != null) {
                val res: Resources = packageManager.getResourcesForApplication(it.activityInfo.applicationInfo)
                // if activity label res is found
                val name = if (it.activityInfo.labelRes != 0) res.getString(it.activityInfo.labelRes)
                else it.activityInfo.applicationInfo.loadLabel(packageManager).toString()
                this.packageAndAppNameMap[it.activityInfo.packageName] = name

                val installedAppsData: InstalledAppsData =
                    InstalledAppsData(name, it.activityInfo.packageName, null)

                val applicationInfo: ApplicationInfo = it.activityInfo.applicationInfo;

                val category: String? = AppCategoryUtils.getAppCategoryFromApplicationInfo(context, applicationInfo);
                installedAppsData.category = category

                installedPackagesList.add(installedAppsData)
            }
        }
        return installedPackagesList
    }


    fun getDetailedAppsAndServices(packageManager: PackageManager): ArrayList<InstalledAppsData> {
        val pm = packageManager
        val apps: ArrayList<InstalledAppsData> = ArrayList<InstalledAppsData>()
        val packs = packageManager.getInstalledPackages(0)
        //List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (i in packs.indices) {
            val p = packs[i]
            val appName = p.applicationInfo.loadLabel(packageManager).toString()
//            val icon = p.applicationInfo.loadIcon(packageManager)
            val packageName = p.applicationInfo.packageName
            if (appName !== packageName)

                this.packageAndAppNameMap.put(packageName, appName)
            apps.add(InstalledAppsData(appName, packageName, isSystemPackage(p)))
        }
        return apps
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun getCurrentLauncherPackageName(context: Context): String {
        /**
         * Main Function to get the current launcher name of the android system to not record that time.
         */
        if (this.currentLauncherName.isNullOrBlank()) this.getCurrentLauncherPackageNameUtils(context)
        return this.currentLauncherName!!;
    }

    private fun getCurrentLauncherPackageNameUtils(context: Context) {
        /**
         * Function to get the current launcher name of the android system to not record that time.
         * THis function sets the global variable only one time.
         */
        val pm: PackageManager = context.packageManager;
        val intent: Intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo: ResolveInfo? = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        this.currentLauncherName =  resolveInfo!!.activityInfo.packageName ?: "";
    }
}