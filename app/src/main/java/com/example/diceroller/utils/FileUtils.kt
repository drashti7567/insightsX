package com.example.diceroller.utils

import android.content.Context
import java.io.*

object FileUtils {
    
    fun emptyFileContents(context: Context, fileName: String, initialContent: String?) {
        val fOut: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        val contentToWriteInEmptyFile: String = initialContent ?: ""
        fOut.write(contentToWriteInEmptyFile.toByteArray())
        fOut.close()
    }
    
    fun writeFileOnInternalStorage(context: Context, fileName: String, sBody: String) {
        val fOut: FileOutputStream = context.openFileOutput(fileName, Context.MODE_APPEND)
        fOut.write((sBody).toByteArray())
        fOut.close()
    }

    fun readFileOnInternalStorage(context: Context, fileName: String?): String {
        val fileText = context.openFileInput(fileName).bufferedReader().useLines { lines ->
            lines.fold("") { line1, line2 -> line1 + line2 }
        }
        return fileText
    }
}