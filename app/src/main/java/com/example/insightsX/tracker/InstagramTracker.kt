package com.example.insightsX.tracker

import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.insightsX.constants.InstagramContentType
import com.example.insightsX.constants.InstagramViewIdConstants
import com.example.insightsX.database.InstagramDataDbHandler
import com.example.insightsX.models.InstagramUsageQueueData
import com.example.insightsX.utils.AudioManagerUtils
import com.example.insightsX.utils.MiscUtils
import com.example.insightsX.utils.NodeInfoUtils
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object InstagramTracker {

    lateinit var listOfViewIds: MutableList<String>;
    lateinit var mapOfViewIdsWithText: HashMap<String, String>;

    var instaUsageQueue: ArrayDeque<InstagramUsageQueueData> = ArrayDeque()

    private fun initializeViewIdsList() {
        this.listOfViewIds = ArrayList<String>();
        this.mapOfViewIdsWithText = HashMap<String, String>()
    }

    fun onEventReceived(event: AccessibilityEvent, context: Context) {

        /**
         * Driver Function that is called from accessibility service
         * when Instagram event is called
         * @param event: Accessibility event
         * @param context: context of application
         */


        val source: AccessibilityNodeInfo = event.source ?: return;
        this.initializeViewIdsList()

        val parent: AccessibilityNodeInfo? = NodeInfoUtils.getParent(source)
        NodeInfoUtils.getListOfViewIds(parent, this.listOfViewIds, this.mapOfViewIdsWithText)

        val usageElement: InstagramUsageQueueData = this.getUsageElement()
        this.pushToQueue(usageElement, context)
    }

    private fun getUsageElement(): InstagramUsageQueueData {
        /**
         * Function to get the instagram usage element which sets the
         * content type of insta content that user is currently watching
         */
        val currentDate: Date = Date()
        val usageElement: InstagramUsageQueueData = InstagramUsageQueueData(
            MiscUtils.dayOfWeekFormat.format(currentDate), MiscUtils.dateFormat.format(currentDate))

        if (this.listOfViewIds.any { it in InstagramViewIdConstants.storiesViewIds }) {
            usageElement.contentType = InstagramContentType.STORIES
        }
        else if (this.listOfViewIds.any { it in InstagramViewIdConstants.reelsViewIds }) {
            usageElement.contentType = InstagramContentType.REELS
        }
        else if (this.listOfViewIds.containsAll(InstagramViewIdConstants.instaBrowserViewIds)) {
            usageElement.contentType = InstagramContentType.BROWSER
        }
        else if (this.listOfViewIds.any { it in InstagramViewIdConstants.profileViewIds } ||
            (this.listOfViewIds.contains(InstagramViewIdConstants.ACTION_BAR_TITLE) &&
                    this.mapOfViewIdsWithText[InstagramViewIdConstants.ACTION_BAR_TITLE]!!
                        .equals(InstagramViewIdConstants.profileActionBarTitle, true))) {
            usageElement.contentType = InstagramContentType.PROFILE
        }
        else if (this.listOfViewIds.containsAll(InstagramViewIdConstants.chatRoomViewIds)) {
            usageElement.contentType = InstagramContentType.DMS
        }
        else if (this.listOfViewIds.any { it in InstagramViewIdConstants.feedViewIds }) {
            usageElement.contentType = InstagramContentType.FEED

        }
        else {
            usageElement.contentType = InstagramContentType.UNKNOWN
        }
        return usageElement
    }

    private fun pushToQueue(usageElement: InstagramUsageQueueData, context: Context) {
        if (AudioManagerUtils.checkIfCallIsActive(context)) {
            if (this.instaUsageQueue.size > 0)
                this.instaUsageQueue.last().endTime = MiscUtils.dateFormat.format(Date())
        }
        else {
            if (this.instaUsageQueue.size == 0) {
                this.instaUsageQueue.addLast(usageElement)
            }
            else if (this.instaUsageQueue.size > 0) {
                if (usageElement.contentType != this.instaUsageQueue.last().contentType) {
                    if (this.instaUsageQueue.last().endTime == null)
                        this.instaUsageQueue.last().endTime = MiscUtils.dateFormat.format(Date())
                    this.instaUsageQueue.addLast(usageElement)
                }
                else {
                    if (this.instaUsageQueue.last().endTime != null) {
                        this.instaUsageQueue.addLast(usageElement)
                    }
                }
            }
        }
    }


    fun writeUsageDataToFile(context: Context) {
        val instaUsageList: ArrayList<InstagramUsageQueueData> = ArrayList()
        while (this.instaUsageQueue.size > 0) {
            val instaUsageElement: InstagramUsageQueueData = this.instaUsageQueue.first()
            if (instaUsageElement.endTime == null) {
                instaUsageElement.endTime = MiscUtils.dateFormat.format(Date())
            }
            instaUsageList.add(instaUsageElement)
            this.instaUsageQueue.removeFirst()

        }
        val dbHandler = InstagramDataDbHandler(context).getInstance(context)
        dbHandler!!.addMultipleInstagramData(instaUsageList)
//        this.sendDataToServer(context)
    }

    fun onDestroy(context: Context) {
        this.writeUsageDataToFile(context)
    }
}