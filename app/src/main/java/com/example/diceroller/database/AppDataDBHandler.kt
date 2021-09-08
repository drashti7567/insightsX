package com.example.diceroller.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.diceroller.models.AppUsageQueueData

class AppDataDBHandler(context: Context): DBHelper(context) {

    private val DATABASE_NAME = "InsightsX"
    private val ID = "id"
    private val APP_DATA_TABLE = "appData"
    private val PACKAGE_NAME: String = "appPackageName"
    private val APP_NAME: String = "appName"
    private val DAY_OF_WEEK: String = "dayOfWeek"
    private val START_TIME: String = "startTime"
    private val END_TIME: String = "endTime"

    fun addAppData(appDataObj: AppUsageQueueData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(PACKAGE_NAME, appDataObj.appPackageName)
        contentValues.put(APP_NAME, appDataObj.appName)
        contentValues.put(DAY_OF_WEEK, appDataObj.dayOfWeek)
        contentValues.put(START_TIME, appDataObj.startTime)
        contentValues.put(END_TIME, appDataObj.endTime)
        val success = db.insert(APP_DATA_TABLE, null, contentValues)
        db.close()
        return success
    }

    fun addMultipleAppData(appDataList: List<AppUsageQueueData>) {
        appDataList.forEach {usageData -> addAppData(usageData)}
    }

    @SuppressLint("Range")
    fun viewAppData(): ArrayList<AppUsageQueueData> {
        val appUsageList: ArrayList<AppUsageQueueData> = ArrayList<AppUsageQueueData>()
        val selectQuery = "select * from $APP_DATA_TABLE"

        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        if(cursor == null && cursor?.count!! <= 0) return appUsageList

        if (cursor.moveToFirst()) {
            do {
                val appUsageQueueData =
                    AppUsageQueueData(
                        appPackageName = cursor.getString(cursor.getColumnIndex(PACKAGE_NAME)),
                        appName = cursor.getString(cursor.getColumnIndex(APP_NAME)),
                        dayOfWeek = cursor.getString(cursor.getColumnIndex(DAY_OF_WEEK)),
                        startTime = cursor.getString(cursor.getColumnIndex(START_TIME)),
                        endTime = cursor.getString(cursor.getColumnIndex(END_TIME))
                    )
                appUsageQueueData.id = cursor.getInt(cursor.getColumnIndex(ID))
                appUsageList.add(appUsageQueueData)
            } while (cursor.moveToNext())
        }

        return appUsageList
    }

    fun deleteAppData(appDataid: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(APP_DATA_TABLE, "id=$appDataid", null)
        db.close()
        return success
    }

    fun deleteMultipleAppData(appDataList: List<AppUsageQueueData>): Int {
        val db = this.writableDatabase
        val commaSeparatedIds = appDataList.map {data -> data.id}
            .joinToString()
        val success = db.delete(APP_DATA_TABLE, "id in ($commaSeparatedIds)", null)
        db.close()
        return success
    }
}