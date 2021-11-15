package com.example.insightsX.utils

import android.os.Build

object PhoneDetailsUtils {

    fun getPhoneModel(): String {
        val manufacturer: String = Build.MANUFACTURER
        var model: String = Build.MODEL
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase()))
            model = model.toLowerCase().replace(manufacturer.toLowerCase(), "").trim()
        return model
    }

    fun getPhoneManufacturer(): String {
        return Build.MANUFACTURER
    }
}