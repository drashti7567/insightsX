package com.example.insightsX.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SharedPreferencesUtils {

    private const val MEMBER_ID = ""
    private const val APPS_UPLOADED = "appsUploaded"

    fun getSharedPreferences(ctx: Context?): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    fun setMemberId(ctx: Context?, memberId: String?) {
        val editor: SharedPreferences.Editor = getSharedPreferences(ctx).edit()
        editor.putString(MEMBER_ID, memberId)
        editor.commit()
    }

    fun getMemberId(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(MEMBER_ID, "")
    }

    fun setInstalledAppsUploaded(ctx: Context?, appsUploaded: Boolean) {
        val editor: SharedPreferences.Editor = getSharedPreferences(ctx).edit()
        editor.putBoolean(APPS_UPLOADED, appsUploaded)
        editor.commit()
    }

    fun getInstalledAppsUploaded(ctx: Context?): Boolean? {
        return getSharedPreferences(ctx).getBoolean(APPS_UPLOADED, false)
    }
}