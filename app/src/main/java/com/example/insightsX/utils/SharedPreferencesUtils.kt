package com.example.insightsX.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SharedPreferencesUtils {

    private const val MEMBER_ID = "memberId"

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
}