package com.example.insightsX.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object AppCategoryUtils {

    const val TAG = "AppCategoryUtils"
    const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="

    lateinit var currentPackageName: String

    fun getAppCategoryFromApplicationInfo(context: Context, applicationInfo: ApplicationInfo): String? {
        /**
         * Given application package name, this functions return
         * the category name defined in application info of app package
         */
        var category: String? = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appCategoryId: Int = applicationInfo.category;
            category =
                if (appCategoryId == -1) null
                else ApplicationInfo.getCategoryTitle(context, appCategoryId).toString();
        }

        return category;
    }

    fun getCategoryFromPlayStore(context: Context, packageName: String): String? {
        if (MiscUtils.isNetworkConnected(context)) {
            try {
                this.currentPackageName = packageName;
                    val category: String? = MyTask().execute().toString()
                return category
            }
            catch (e: Exception) {
                Log.d(TAG, e.printStackTrace().toString())
                return null
            }
        }
        return null;
    }

    private class MyTask: AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg p0: Void?): String? {
            val doc: Document = Jsoup.connect(GOOGLE_URL + currentPackageName).get()
            val link: Element = doc.select("a[itemprop=\"genre\"]").first()
            return link.text()
        }
    }
}