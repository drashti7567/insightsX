package com.example.diceroller.utils

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi

object PowerManagerUtils {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun checkIfPowerSaverIsOn(context: Context): Boolean {
        val powerManager: PowerManager = context.getSystemService((Context.POWER_SERVICE)) as PowerManager
        return powerManager.isPowerSaveMode
    }
}