package com.example.insightsX.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build




open class DBHelper(context: Context?): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "InsightsX"
        private val APP_DATA_TABLE = "appData"
        private val YOUTUBE_DATA_TABLE = "youtubeData"
        private val INSTAGRAM_ADS_DATA_TABLE = "instagramAdsData"
        private val INSTAGRAM_DATA_TABLE = "instagramData"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "create table if not exists $APP_DATA_TABLE " +
                    "(id integer primary key autoincrement, appPackageName text,appName text," +
                    "dayOfWeek text, startTime text,endTime text, synced integer default 0)"
        );
        db?.execSQL(
            "create table if not exists $YOUTUBE_DATA_TABLE " +
                    "(id integer primary key autoincrement, contentType text,videoName text,videoChannelName text," +
                    "adName text,adSkipped integer, dayOfWeek text," +
                    " startTime text,endTime text, synced integer default 0)"
        );
        db?.execSQL(
            "create table if not exists $INSTAGRAM_DATA_TABLE " +
                    "(id integer primary key autoincrement, contentType text, dayOfWeek text," +
                    " startTime text,endTime text, synced integer default 0)"
        );
        db?.execSQL(
            "create table if not exists $INSTAGRAM_ADS_DATA_TABLE" +
                    "(id integer primary key autoincrement, contentType text, adsCompany text," +
                    " adsDescription text, adsDescription2 text, time text, synced integer default 0)"
        );
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $APP_DATA_TABLE" )
        db!!.execSQL("DROP TABLE IF EXISTS $YOUTUBE_DATA_TABLE" )
        db!!.execSQL("DROP TABLE IF EXISTS $INSTAGRAM_DATA_TABLE" )
        db!!.execSQL("DROP TABLE IF EXISTS $INSTAGRAM_ADS_DATA_TABLE" )
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging()
        }
    }


}