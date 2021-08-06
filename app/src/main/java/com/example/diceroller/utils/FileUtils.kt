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
    
    fun writeFileOnInternalStorage(context: Context, fileName: String, sBody: String, initialContent: String?) {
        val file: File = context.getFileStreamPath(fileName);
        if(!file.exists()) {
            this.emptyFileContents(context, fileName, initialContent)
        }
        val fOut: FileOutputStream = context.openFileOutput(fileName, Context.MODE_APPEND)
        fOut.write((sBody).toByteArray())
        fOut.close()
    }

    fun readFileOnInternalStorage(context: Context, fileName: String?): String {
        val file: File = context.getFileStreamPath(fileName);
        if(file.exists()) {
            return context.openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.fold("") { line1, line2 -> line1 + line2 }
            }
        }
        return ""
    }

    fun getFileContentAsList(context: Context, fileName: String?): MutableList<String> {
        val fileTextList: MutableList<String> = ArrayList()
        val file: File = context.getFileStreamPath(fileName);
        if(file.exists()) {
            context.openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    run {
                        fileTextList.add(line)
                    }
                }
            }
        }
        return fileTextList
    }
}