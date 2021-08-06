package com.example.diceroller.detectors

import android.content.Context
import com.example.diceroller.utils.MiscUtils

object AppChecker {

    private var detector: Detector? = null

    fun getForegroundApp(context: Context?): String {
        return detector!!.getForegroundApp(context)
    }

    init {
        detector = if (MiscUtils.postLollipop()) LollipopDetector() else PreLollipopDetector()
    }
}