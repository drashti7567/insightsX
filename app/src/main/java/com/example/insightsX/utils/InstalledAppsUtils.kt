package com.example.insightsX.utils

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import com.example.insightsX.models.InstalledAppsData

object InstalledAppsUtils {

    var packageAndAppNameMap = HashMap<String, String>();

    fun getInstalledApps(packageManager: PackageManager): ArrayList<InstalledAppsData> {
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
                installedPackagesList.add(InstalledAppsData(name, it.activityInfo.packageName, null))
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
}