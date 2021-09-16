package com.example.insightsX.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.insightsX.models.InstagramAdsData

class InstagramAdsDbHandler(context: Context): DBHelper(context) {
    private val DATABASE_NAME = "InsightsX"
    private val ID = "id"
    private val INSTAGRAM_ADS_TABLE_NAME = "instagramAdsData"
    private val CONTENT_TYPE = "contentType"
    private val AD_COMPANY = "adsCompany"
    private val AD_DESCRIPTION = "adsDescription"
    private val AD_DESCRIPTION2 = "adsDescription"
    private val TIME = "time"

    private var dbInstance: InstagramAdsDbHandler? = null

    fun getInstance(context: Context): InstagramAdsDbHandler? {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (dbInstance == null) {
            dbInstance = InstagramAdsDbHandler(context.applicationContext)
        }
        return dbInstance
    }

    fun addAdsInstagramData(instaAdsDataObj: InstagramAdsData): Long {
        val db = this.writableDatabase
        this.onCreate(db)
        val contentValues = ContentValues()
        contentValues.put(CONTENT_TYPE, instaAdsDataObj.contentType)
        contentValues.put(ID, instaAdsDataObj.id)
        contentValues.put(AD_COMPANY, instaAdsDataObj.adCompany)
        contentValues.put(AD_DESCRIPTION, instaAdsDataObj.adDescription)
        contentValues.put(AD_DESCRIPTION2, instaAdsDataObj.adDescription2)
        contentValues.put(TIME, instaAdsDataObj.time)
        val success = db.insert(INSTAGRAM_ADS_TABLE_NAME, null, contentValues)
        db.close()
        return success
    }

    fun addMultipleAdData(instaAdsDataList: List<InstagramAdsData>) {
        instaAdsDataList.forEach {usageData -> addAdsInstagramData(usageData)}
    }

    @SuppressLint("Range")
    fun viewInstagramAdsData(): ArrayList<InstagramAdsData> {
        val adsList: ArrayList<InstagramAdsData> = ArrayList<InstagramAdsData>()
        val selectQuery = "select * from $INSTAGRAM_ADS_TABLE_NAME"

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
            return adsList
        }

        if (cursor.moveToFirst()) {
            do {
                val instaAdObj = InstagramAdsData()
                instaAdObj.contentType = cursor.getString(cursor.getColumnIndex(CONTENT_TYPE))
                instaAdObj.adCompany = cursor.getString(cursor.getColumnIndex(AD_COMPANY))
                instaAdObj.adDescription = cursor.getString(cursor.getColumnIndex(AD_DESCRIPTION))
                instaAdObj.adDescription2 = cursor.getString(cursor.getColumnIndex(AD_DESCRIPTION2))
                instaAdObj.id = cursor.getInt(cursor.getColumnIndex(ID))
                instaAdObj.time = cursor.getString(cursor.getColumnIndex(TIME))
                adsList.add(instaAdObj)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor!!.close()
        return adsList
    }

    fun deleteAppData(instaAdDataId: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(INSTAGRAM_ADS_TABLE_NAME, "id=$instaAdDataId", null)
        db.close()
        return success
    }

    fun deleteMultipleAppData(instaAdsDataList: List<InstagramAdsData>): Int {
        val db = this.writableDatabase
        val commaSeparatedIds = instaAdsDataList.map {data -> data.id}
            .joinToString()
        val success = db.delete(INSTAGRAM_ADS_TABLE_NAME, "id in ($commaSeparatedIds)", null)
        db.close()
        return success
    }
}