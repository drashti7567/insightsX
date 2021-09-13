package com.example.insightsX.utils

import android.content.Context
import android.content.res.AssetManager
import java.io.InputStream
import java.util.*


object PropertyReaderUtils {

    private var properties: Properties? = Properties()

    fun getMyProperties(file: String?, context: Context): Properties? {
        if(properties == null) properties = Properties()
        try {
            val assetManager: AssetManager = context!!.assets
            val inputStream: InputStream = assetManager.open(file!!)
            properties!!.load(inputStream)
        }
        catch (e: Exception) {
            print(e.message)
        }
        return properties
    }
}