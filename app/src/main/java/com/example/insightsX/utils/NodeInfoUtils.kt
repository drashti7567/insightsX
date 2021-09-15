package com.example.insightsX.utils

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

object NodeInfoUtils {
    fun getParent(nodeInfo: AccessibilityNodeInfo?):AccessibilityNodeInfo? {
        if (nodeInfo != null) {
            return if (nodeInfo.parent != null) getParent(nodeInfo.parent) else nodeInfo
        }
        return null
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    fun logViewHierarchy(nodeInfo: AccessibilityNodeInfo?, depth: Int) {
        if (nodeInfo == null) return
        var spacerString = ""
        for (i in 0 until depth) {
            spacerString += '-'
        }
        Log.d(
            "TAG",
            spacerString + nodeInfo.className + " " + nodeInfo.text + " " + nodeInfo.viewIdResourceName
        )
        for (i in 0 until nodeInfo.childCount) {
            logViewHierarchy(nodeInfo.getChild(i), depth + 1)
        }
    }

    fun getListOfViewIds(nodeInfo: AccessibilityNodeInfo?, listOfViewIds: MutableList<String>,
                                 mapOfViewIdsWithText: HashMap<String, String>) {
        if (nodeInfo == null) return
        if (nodeInfo.viewIdResourceName != null && nodeInfo.viewIdResourceName.contains("/")) {
            listOfViewIds.add(nodeInfo.viewIdResourceName.split("/")[1])
            if (nodeInfo.text != null)
                mapOfViewIdsWithText[nodeInfo.viewIdResourceName.split("/")[1]] =
                    mapOfViewIdsWithText[nodeInfo.viewIdResourceName.split("/")[1]] ?: "" + nodeInfo.text.toString()
        }
        for (i in 0 until nodeInfo.childCount) {
            getListOfViewIds(nodeInfo.getChild(i), listOfViewIds, mapOfViewIdsWithText)
        }

    }

}