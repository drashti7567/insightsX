package com.example.insightsX.receivers;

import android.R
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log


class AudioPlaybackReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra("track")!!.replace("'".toRegex(), "''")
        val c: Cursor? = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATA), MediaStore.Audio.Media.TITLE + "='" + path + "'",
            null, null, 0)
        try {
            if (c == null || c.getCount() === 0) return
            val size: Int = c.getCount()
            if (size != 1) return
            c.moveToNext()

            // Here's the song path
            val songPath: String = c.getString(0)
            Log.d("ROAST_APP", "" + songPath)
        }
        finally {
            if (c != null) c.close()
        }
    }

    fun query(context: Context, uri: Uri, projection: Array<String?>?, selection: String?,
              selectionArgs: Array<String?>?, sortOrder: String?, limit: Int): Cursor? {

        var uri: Uri = uri
        return try {
            val resolver: ContentResolver = context.getContentResolver() ?: return null
            if (limit > 0) uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build()
            resolver.query(uri, projection, selection, selectionArgs, sortOrder)
        }
        catch (ex: UnsupportedOperationException) {
            null
        }
    }
}
