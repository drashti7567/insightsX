package com.example.diceroller.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class DBHelper(context: Context?): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "InsightsX"
        private val APP_DATA_TABLE = "appData"
        private val YOUTUBE_DATA_TABLE = "youtubeData"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "create table $APP_DATA_TABLE " +
                    "(id integer primary key autoincrement, appPackageName text,appName text," +
                    "dayOfWeek text, startTime text,endTime text, synced integer default 0)"
        );
        db?.execSQL(
            "create table $YOUTUBE_DATA_TABLE " +
                    "(id integer primary key autoincrement, contentType text,videoName text,videoChannelName text," +
                    "adName text,adSkipped integer, dayOfWeek text," +
                    " startTime text,endTime text, synced integer default 0)"
        );
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $APP_DATA_TABLE" )
        db!!.execSQL("DROP TABLE IF EXISTS $YOUTUBE_DATA_TABLE" )
        onCreate(db)
    }


}