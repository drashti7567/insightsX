package com.example.insightsX.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.insightsX.models.YoutubeUsageQueueData

class YoutubeDataDbHandler(context: Context): DBHelper(context) {
    private val DATABASE_NAME = "InsightsX"
    private val ID = "id"
    private val YOUTUBE_TABLE_NAME = "youtubeData"
    private val CONTENT_TYPE = "contentType"
    private val VIDEO_NAME: String = "videoName"
    private val VIDEO_CHANNEL_NAME: String = "videoChannelName"
    private val AD_NAME = "adName"
    private val AD_SKIPPED: String = "adSkipped"
    private val DAY_OF_WEEK: String = "dayOfWeek"
    private val START_TIME: String = "startTime"
    private val END_TIME: String = "endTime"

    fun addYoutubeData(youtubeDataObj: YoutubeUsageQueueData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(CONTENT_TYPE, youtubeDataObj.contentType)
        contentValues.put(VIDEO_NAME, youtubeDataObj.videoName)
        contentValues.put(VIDEO_CHANNEL_NAME, youtubeDataObj.videoChannelName)
        contentValues.put(AD_NAME, youtubeDataObj.adName)
//        contentValues.put(AD_SKIPPED, youtubeDataObj.adSkipped)
        contentValues.put(ID, youtubeDataObj.id)
        contentValues.put(DAY_OF_WEEK, youtubeDataObj.dayOfWeek)
        contentValues.put(START_TIME, youtubeDataObj.startTime)
        contentValues.put(END_TIME, youtubeDataObj.endTime)
        val success = db.insert(YOUTUBE_TABLE_NAME, null, contentValues)
        db.close()
        return success
    }

    fun addMultipleYoutubeData(youtubeDataList: List<YoutubeUsageQueueData>) {
        youtubeDataList.forEach {usageData -> addYoutubeData(usageData)}
    }

    @SuppressLint("Range")
    fun viewYoutubeData(): ArrayList<YoutubeUsageQueueData> {
        val appUsageList: ArrayList<YoutubeUsageQueueData> = ArrayList<YoutubeUsageQueueData>()
        val selectQuery = "select * from $YOUTUBE_TABLE_NAME"

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
                val youtubeUsageQueueData =
                    YoutubeUsageQueueData(
                        dayOfWeek = cursor.getString(cursor.getColumnIndex(DAY_OF_WEEK)),
                        startTime = cursor.getString(cursor.getColumnIndex(START_TIME)),
                    )
                youtubeUsageQueueData.contentType = cursor.getString(cursor.getColumnIndex(CONTENT_TYPE))
                youtubeUsageQueueData.videoName = cursor.getString(cursor.getColumnIndex(VIDEO_NAME))
                youtubeUsageQueueData.videoChannelName = cursor.getString(cursor.getColumnIndex(VIDEO_CHANNEL_NAME))
                youtubeUsageQueueData.adName = cursor.getString(cursor.getColumnIndex(AD_NAME))
//                youtubeUsageQueueData.adSkipped = cursor.getInt(cursor.getColumnIndex(AD_SKIPPED))
                youtubeUsageQueueData.endTime = cursor.getString(cursor.getColumnIndex(END_TIME))
                youtubeUsageQueueData.id = cursor.getInt(cursor.getColumnIndex(ID))
                appUsageList.add(youtubeUsageQueueData)
            } while (cursor.moveToNext())
        }

        return appUsageList
    }

    fun deleteAppData(youtubeDataId: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(YOUTUBE_TABLE_NAME, "id=$youtubeDataId", null)
        db.close()
        return success
    }

    fun deleteMultipleAppData(youtubeDataList: List<YoutubeUsageQueueData>): Int {
        val db = this.writableDatabase
        val commaSeparatedIds = youtubeDataList.map {data -> data.id}
            .joinToString()
        val success = db.delete(YOUTUBE_TABLE_NAME, "id in ($commaSeparatedIds)", null)
        db.close()
        return success
    }
}