package com.example.diceroller.tracker

import android.content.Context
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.diceroller.activities.LifeCycleActivity
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.constants.YoutubeContentType
import com.example.diceroller.constants.YoutubeViewIdConstants
import com.example.diceroller.models.YoutubeUsageQueueData
import com.example.diceroller.utils.AudioManagerUtils
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.MiscUtils
import com.example.diceroller.utils.NodeInfoUtils
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object YoutubeTracker {

    lateinit var listOfViewIds: MutableList<String>;
    lateinit var mapOfViewIdsWithText: HashMap<String, String>;

    var youtubeUsageQueue: ArrayDeque<YoutubeUsageQueueData> = ArrayDeque()

    private fun pushToQueue(usageElement: YoutubeUsageQueueData, context: Context) {
        if (AudioManagerUtils.checkIfCallIsActive(context)) {
            if (this.youtubeUsageQueue.size > 0)
                this.youtubeUsageQueue.last().endTime = MiscUtils.dateFormat.format(Date())
        }
        else {
            if (this.youtubeUsageQueue.size == 0) {
                this.youtubeUsageQueue.addLast(usageElement)
            }
            else if (this.youtubeUsageQueue.size > 0) {
                if (usageElement.contentType != this.youtubeUsageQueue.last().contentType) {
                    if(this.youtubeUsageQueue.last().endTime == null)
                        this.youtubeUsageQueue.last().endTime = MiscUtils.dateFormat.format(Date())
                    this.youtubeUsageQueue.addLast(usageElement)
                }
                else {
                    if (this.youtubeUsageQueue.last().endTime != null) {
                        this.youtubeUsageQueue.addLast(usageElement)
                    }
                    else {
                        if (usageElement.contentType == YoutubeContentType.YOUTUBE_VIDEO) {
                            if (usageElement.videoName != null && usageElement.videoName != "") {
                                if (!usageElement.videoName.equals(this.youtubeUsageQueue.last().videoName, true)) {
                                    this.youtubeUsageQueue.last().endTime = MiscUtils.dateFormat.format(Date())
                                    this.youtubeUsageQueue.addLast(usageElement)
                                }
                                else {
                                    this.youtubeUsageQueue.last().videoChannelName =
                                        usageElement.videoChannelName ?: this.youtubeUsageQueue.last().videoChannelName
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getYoutubeUsageElement(): YoutubeUsageQueueData {
        val currentDate: Date = Date()
        val usageElement: YoutubeUsageQueueData = YoutubeUsageQueueData(
            MiscUtils.dayOfWeekFormat.format(currentDate), MiscUtils.dateFormat.format(currentDate))

        if (this.listOfViewIds.containsAll(YoutubeViewIdConstants.floatyVideoIds) &&
            !this.listOfViewIds.containsAll(YoutubeViewIdConstants.floatyAdsIds)) {

            usageElement.contentType = YoutubeContentType.YOUTUBE_VIDEO
            usageElement.videoName =
                this.mapOfViewIdsWithText[YoutubeViewIdConstants.YOUTUBE_FLOATY_VIDEO_NAME_ID] ?: ""
            usageElement.videoChannelName =
                this.mapOfViewIdsWithText[YoutubeViewIdConstants.YOUTUBE_FLOATY_CHANNEL_NAME] ?: ""
            Handler().postDelayed({
                LifeCycleActivity.allowWindowContentChangeEvent = false
            }, 1000)

        }
        else if (this.listOfViewIds.containsAll(YoutubeViewIdConstants.floatyAdsIds)
            && this.listOfViewIds.containsAll(YoutubeViewIdConstants.floatyVideoIds)) {
            usageElement.contentType = YoutubeContentType.YOUTUBE_AD
            usageElement.adName = this.mapOfViewIdsWithText[YoutubeViewIdConstants.YOUTUBE_FLOATY_AD_NAME] ?: ""

        }
        else if (this.listOfViewIds.any { it in YoutubeViewIdConstants.youtubeAdIds }) {
            usageElement.contentType = YoutubeContentType.YOUTUBE_AD
            usageElement.adName = this.mapOfViewIdsWithText[YoutubeViewIdConstants.AD_NAME] ?: ""
        }
        else if (this.listOfViewIds.containsAll(YoutubeViewIdConstants.youtubeVideoIds)) {
            usageElement.contentType = YoutubeContentType.YOUTUBE_VIDEO
            usageElement.videoName = this.mapOfViewIdsWithText[YoutubeViewIdConstants.YOUTUBE_VIDEO_NAME_ID] ?: ""
            usageElement.videoChannelName = this.mapOfViewIdsWithText[YoutubeViewIdConstants.YOUTUBE_CHANNEL_NAME] ?: ""
            Handler().postDelayed({
                LifeCycleActivity.allowWindowContentChangeEvent = false
            }, 1000)
        }
        else if (this.listOfViewIds.any { it in YoutubeViewIdConstants.reelsIds }) {
            usageElement.contentType = YoutubeContentType.SHORTS
            LifeCycleActivity.allowWindowContentChangeEvent = false
        }
        else {
            usageElement.contentType = YoutubeContentType.BROWSE_CONTENT
            LifeCycleActivity.allowWindowContentChangeEvent = false
        }

        return usageElement
    }

    private fun isLiveVideoDuplicateEvent(): Boolean {
        if (this.listOfViewIds.any{
                mutableListOf(YoutubeViewIdConstants.LIVE_LABEL, YoutubeViewIdConstants.LIVE_CHAT_VEM).contains(it)}) {
            return true
        }
        return false
    }

    private fun initializeViewIdsList() {
        this.listOfViewIds = ArrayList();
        this.mapOfViewIdsWithText = HashMap<String, String>()
    }

    fun onYoutubeEventReceived(event: AccessibilityEvent, context: Context) {
        val source: AccessibilityNodeInfo = event.source ?: return;
        this.initializeViewIdsList()

        val parent: AccessibilityNodeInfo? = NodeInfoUtils.getParent(source)
        NodeInfoUtils.getListOfViewIds(parent, this.listOfViewIds, this.mapOfViewIdsWithText)
        if (this.isLiveVideoDuplicateEvent()) return
        val usageElement: YoutubeUsageQueueData = getYoutubeUsageElement()
        this.pushToQueue(usageElement, context)
    }

    fun writeYoutubeUsageDataToFile(context: Context) {
        var csvChunk: String = ""
        while (this.youtubeUsageQueue.size > 0) {
            val youtubeUsageElement: YoutubeUsageQueueData = this.youtubeUsageQueue.first()
            if (youtubeUsageElement.endTime == null) {
                youtubeUsageElement.endTime = MiscUtils.dateFormat.format(Date())
            }
            val csvRow: String =
                youtubeUsageElement.contentType + "," +
                        (youtubeUsageElement.videoName?.replace(",", "|") ?: "") + "," +
                        (youtubeUsageElement.videoChannelName?.replace(",", "|") ?: "") + "," +
                        youtubeUsageElement.dayOfWeek + "," +
                        youtubeUsageElement.startTime + "," + youtubeUsageElement.endTime + "\n"
            csvChunk += csvRow
            this.youtubeUsageQueue.removeFirst()

        }
        FileUtils.writeFileOnInternalStorage(
            context,
            FileNameConstants.YOUTUBE_USAGE_FILE_NAME,
            csvChunk
        )
    }

    fun onDestroy(context: Context) {
        writeYoutubeUsageDataToFile(context)
    }
}