package com.example.insightsX.tracker

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.insightsX.activities.LifeCycleActivity
import com.example.insightsX.constants.ApiUrlConstants
import com.example.insightsX.constants.YoutubeContentType
import com.example.insightsX.constants.YoutubeViewIdConstants
import com.example.insightsX.database.YoutubeDataDbHandler
import com.example.insightsX.models.YoutubeUsageQueueData
import com.example.insightsX.utils.AudioManagerUtils
import com.example.insightsX.utils.HttpUtils
import com.example.insightsX.utils.MiscUtils
import com.example.insightsX.utils.NodeInfoUtils
import com.example.insightsX.utils.SharedPreferencesUtils
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
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
                    if (this.youtubeUsageQueue.last().endTime == null)
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
            Handler(Looper.getMainLooper()).postDelayed({
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
            Handler(Looper.getMainLooper()).postDelayed({
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
        if (this.listOfViewIds.any {
                mutableListOf(YoutubeViewIdConstants.LIVE_LABEL, YoutubeViewIdConstants.LIVE_CHAT_VEM).contains(it)
            }) {
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
        val youtubeUsageList: ArrayList<YoutubeUsageQueueData> = ArrayList()
        while (this.youtubeUsageQueue.size > 0) {
            val youtubeUsageElement: YoutubeUsageQueueData = this.youtubeUsageQueue.first()
            if (youtubeUsageElement.endTime == null) {
                youtubeUsageElement.endTime = MiscUtils.dateFormat.format(Date())
            }
            youtubeUsageList.add(youtubeUsageElement)
            this.youtubeUsageQueue.removeFirst()

        }
        val dbHandler = YoutubeDataDbHandler(context)
        dbHandler.addMultipleYoutubeData(youtubeUsageList)

        this.sendYoutubeDataToServer(context)

    }

    private fun sendYoutubeDataToServer(context: Context) {

        val youtubeTrackerContext = this;

//        CoroutineScope(Dispatchers.Default).launch {
            val dbHelper = YoutubeDataDbHandler(context)
            val youtubeDataList: ArrayList<YoutubeUsageQueueData> = dbHelper.viewYoutubeData()

            val entity: StringEntity = youtubeTrackerContext.createPostRequestBody(context, youtubeDataList)

            HttpUtils.post(context, ApiUrlConstants.addYoutubeDataToServer, entity, "application/json",
                object: AsyncHttpResponseHandler(true) {

                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        try {
                            val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                            if (serverResp.get("success") == true) {
                                dbHelper.deleteMultipleAppData(youtubeDataList)
                            }
                        }
                        catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseBody: ByteArray?,
                        error: Throwable?) {
                        Log.d("Error", error.toString() + " " + responseBody.toString())
                    }
                })
    }

    private fun createPostRequestBody(context: Context,
                                      youtubeDataList: ArrayList<YoutubeUsageQueueData>): StringEntity {
        val requestObj = JSONObject()
        requestObj.put("appName", "youtube")
        requestObj.put("memberId", SharedPreferencesUtils.getMemberId(context))

        val usageDataArray = JSONArray()

        youtubeDataList.forEach { data ->
            val usageObj = JSONObject()
            usageObj.put("videoName", data.videoName)
            usageObj.put("channelName", data.videoChannelName)
            usageObj.put("adName", data.adName)
            usageObj.put("adSkipped", data.adSkipped)
            usageObj.put("contentType", data.contentType)
            usageObj.put("day", data.dayOfWeek)
            usageObj.put("endTime", data.endTime)
            usageObj.put("startTime", data.startTime)
            usageDataArray.put(usageObj)
        }
        requestObj.put("usageData", usageDataArray)

        return StringEntity(requestObj.toString(), "UTF-8")
    }

    fun onDestroy(context: Context) {
        writeYoutubeUsageDataToFile(context)
    }
}