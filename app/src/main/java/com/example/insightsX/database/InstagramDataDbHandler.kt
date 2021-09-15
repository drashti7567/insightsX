package com.example.insightsX.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.insightsX.models.InstagramUsageQueueData

class InstagramDataDbHandler(context: Context): DBHelper(context) {
    private val DATABASE_NAME = "InsightsX"
    private val ID = "id"
    private val INSTAGRAM_TABLE_NAME = "instagramData"
    private val CONTENT_TYPE = "contentType"
    private val DAY_OF_WEEK: String = "dayOfWeek"
    private val START_TIME: String = "startTime"
    private val END_TIME: String = "endTime"

    private var dbInstance: InstagramDataDbHandler? = null

    fun getInstance(context: Context): InstagramDataDbHandler? {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (dbInstance == null) {
            dbInstance = InstagramDataDbHandler(context.applicationContext)
        }
        return dbInstance
    }

    fun addInstagramData(instaDataObj: InstagramUsageQueueData): Long {
        val db = this.writableDatabase
        this.onCreate(db)
        val contentValues = ContentValues()
        contentValues.put(CONTENT_TYPE, instaDataObj.contentType)
        contentValues.put(ID, instaDataObj.id)
        contentValues.put(DAY_OF_WEEK, instaDataObj.dayOfWeek)
        contentValues.put(START_TIME, instaDataObj.startTime)
        contentValues.put(END_TIME, instaDataObj.endTime)
        val success = db.insert(INSTAGRAM_TABLE_NAME, null, contentValues)
        db.close()
        return success
    }

    fun addMultipleInstagramData(instaDataList: List<InstagramUsageQueueData>) {
        instaDataList.forEach {usageData -> addInstagramData(usageData)}
    }

    @SuppressLint("Range")
    fun viewInstagramData(): ArrayList<InstagramUsageQueueData> {
        val appUsageList: ArrayList<InstagramUsageQueueData> = ArrayList<InstagramUsageQueueData>()
        val selectQuery = "select * from $INSTAGRAM_TABLE_NAME"

        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            db.close()
            cursor!!.close()
            return ArrayList()
        }
        if(cursor == null || cursor?.count!! <= 0) {
            cursor.close()
            db.close()
            return appUsageList
        }

        if (cursor.moveToFirst()) {
            do {
                val instaUsageQueueData =
                    InstagramUsageQueueData(
                        dayOfWeek = cursor.getString(cursor.getColumnIndex(DAY_OF_WEEK)),
                        startTime = cursor.getString(cursor.getColumnIndex(START_TIME)),
                    )
                instaUsageQueueData.contentType = cursor.getString(cursor.getColumnIndex(CONTENT_TYPE))
                instaUsageQueueData.endTime = cursor.getString(cursor.getColumnIndex(END_TIME))
                instaUsageQueueData.id = cursor.getInt(cursor.getColumnIndex(ID))
                appUsageList.add(instaUsageQueueData)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor!!.close()
        return appUsageList
    }

    fun deleteAppData(instaDataId: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(INSTAGRAM_TABLE_NAME, "id=$instaDataId", null)
        db.close()
        return success
    }

    fun deleteMultipleAppData(instaDataList: List<InstagramUsageQueueData>): Int {
        val db = this.writableDatabase
        val commaSeparatedIds = instaDataList.map {data -> data.id}
            .joinToString()
        val success = db.delete(INSTAGRAM_TABLE_NAME, "id in ($commaSeparatedIds)", null)
        db.close()
        return success
    }
}