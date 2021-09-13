package com.example.insightsX.detectors

import android.content.Context
import com.example.insightsX.utils.MiscUtils

object AppChecker {

    private var detector: Detector? = null

    fun getForegroundApp(context: Context?): String {
        return detector!!.getForegroundApp(context)
    }

    init {
        detector = if (MiscUtils.postLollipop()) LollipopDetector() else PreLollipopDetector()
    }
}