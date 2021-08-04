package com.example.diceroller.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.diceroller.BuildConfig
import java.io.File

object IntentUtils {
    fun createIntentTOShareCsv(context: Context, fileName: String, appChooserTitle: String) {
        val file: File = File(context.filesDir.absolutePath, fileName)
        val pathUri: Uri =
            FileProvider.getUriForFile(
                context.applicationContext,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Data")
        intent.putExtra(Intent.EXTRA_STREAM, pathUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


        val appChoseIntent: Intent = Intent.createChooser(intent, appChooserTitle)

        val resInfoList = context.packageManager.queryIntentActivities(
            appChoseIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                pathUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        context.startActivity(appChoseIntent)
    }
}