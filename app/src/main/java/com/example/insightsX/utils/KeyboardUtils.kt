package com.example.insightsX.utils

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

object KeyboardUtils {

    fun hideSoftKeyboard(activity: Activity) {
        activity.currentFocus?.let {
            val inputMethodManager = ContextCompat.getSystemService(activity, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}