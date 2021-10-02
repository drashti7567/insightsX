package com.example.insightsX.tracker

import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.insightsX.activities.LifeCycleActivity
import com.example.insightsX.constants.ApiUrlConstants
import com.example.insightsX.constants.InstagramContentType
import com.example.insightsX.constants.InstagramViewIdConstants
import com.example.insightsX.database.InstagramAdsDbHandler
import com.example.insightsX.database.InstagramDataDbHandler
import com.example.insightsX.models.InstagramAdsData
import com.example.insightsX.models.InstagramUsageQueueData
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
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object InstagramTracker {

    lateinit var listOfViewIds: MutableList<String>;
    lateinit var mapOfViewIdsWithText: HashMap<String, String>;

    var instaUsageQueue: ArrayDeque<InstagramUsageQueueData> = ArrayDeque()
    var instaAdsQueue: ArrayDeque<InstagramAdsData> = ArrayDeque()

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

        try {
            val source: AccessibilityNodeInfo = event.source ?: return;
            this.initializeViewIdsList()

            val parent: AccessibilityNodeInfo? = NodeInfoUtils.getParent(source)
            NodeInfoUtils.getListOfViewIds(parent, this.listOfViewIds, this.mapOfViewIdsWithText)

            val (usageElement, adsDataElement) = this.getUsageElement()
            this.pushToQueue(usageElement, adsDataElement, context)
        }
        catch(e: Exception) {
            Log.d("Insta Tracker", e.stackTrace.toString())
        }
    }

    private fun getUsageElement(): Pair<InstagramUsageQueueData, InstagramAdsData> {
        /**
         * Function to get the instagram usage element which sets the
         * content type of insta content that user is currently watching
         */
        val currentDate: Date = Date()
        val usageElement = InstagramUsageQueueData(
            MiscUtils.dayOfWeekFormat.format(currentDate), MiscUtils.dateFormat.format(currentDate))
        val adDataObj = InstagramAdsData()
        adDataObj.time = MiscUtils.dateFormat.format(currentDate)

        if (this.listOfViewIds.any { it in InstagramViewIdConstants.storiesViewIds }) {
            usageElement.contentType = InstagramContentType.STORIES
            adDataObj.contentType = InstagramContentType.STORIES

            if (this.listOfViewIds.contains(InstagramViewIdConstants.storiesSponsoredViewId) &&
                this.mapOfViewIdsWithText[InstagramViewIdConstants.storiesSponsoredViewId]!!
                    .equals(InstagramViewIdConstants.SPONSORED, true)) {
                adDataObj.adCompany = this.mapOfViewIdsWithText[InstagramViewIdConstants.storiesAdCompany]
            }
        }
        else if (this.listOfViewIds.any { it in InstagramViewIdConstants.reelsViewIds }) {

            usageElement.contentType = InstagramContentType.REELS
            adDataObj.contentType = InstagramContentType.REELS

            if (this.listOfViewIds.contains(InstagramViewIdConstants.reelsSponsoredViewId) &&
                this.mapOfViewIdsWithText[InstagramViewIdConstants.reelsSponsoredViewId]!!
                    .equals(InstagramViewIdConstants.SPONSORED, true)) {
                adDataObj.adCompany = this.mapOfViewIdsWithText[InstagramViewIdConstants.reelsAdCompany]
                adDataObj.adDescription = this.mapOfViewIdsWithText[InstagramViewIdConstants.reelsAdDescription]
            }
        }
        else if (this.listOfViewIds.containsAll(InstagramViewIdConstants.instaBrowserViewIds)) {
            usageElement.contentType = InstagramContentType.BROWSER
            adDataObj.contentType = InstagramContentType.BROWSER

            if (this.listOfViewIds.contains(InstagramViewIdConstants.browserTitle) &&
                !this.mapOfViewIdsWithText[InstagramViewIdConstants.browserTitle]!!
                    .contains(InstagramViewIdConstants.LOADING)) {
                adDataObj.adCompany = this.mapOfViewIdsWithText[InstagramViewIdConstants.browserTitle]
                adDataObj.adDescription = this.mapOfViewIdsWithText[InstagramViewIdConstants.browserDescription]
            }
        }
        else if (this.listOfViewIds.any { it in InstagramViewIdConstants.profileViewIds } ||
            (this.listOfViewIds.contains(InstagramViewIdConstants.ACTION_BAR_TITLE) &&
                    this.mapOfViewIdsWithText[InstagramViewIdConstants.ACTION_BAR_TITLE]!!
                        .equals(InstagramViewIdConstants.profileActionBarTitle, true))) {
            usageElement.contentType = InstagramContentType.PROFILE
            adDataObj.contentType = InstagramContentType.PROFILE
        }
        else if (this.listOfViewIds.containsAll(InstagramViewIdConstants.chatRoomViewIds)) {
            usageElement.contentType = InstagramContentType.DMS
            adDataObj.contentType = InstagramContentType.DMS
        }
        else if (this.listOfViewIds.any { it in InstagramViewIdConstants.feedViewIds }) {
            usageElement.contentType = InstagramContentType.FEED
            adDataObj.contentType = InstagramContentType.FEED

            if (this.listOfViewIds.contains(InstagramViewIdConstants.postSponsoredViewId) &&
                this.mapOfViewIdsWithText[InstagramViewIdConstants.postSponsoredViewId]!!
                    .equals(InstagramViewIdConstants.SPONSORED, true)) {
                adDataObj.adCompany = this.mapOfViewIdsWithText[InstagramViewIdConstants.postCompanyAdNameViewId]
                adDataObj.adDescription = this.mapOfViewIdsWithText[InstagramViewIdConstants.postAdDescriptionViewId]
                adDataObj.adDescription2 = this.mapOfViewIdsWithText[InstagramViewIdConstants.postAdDescription2ViewId]
            }

        }
        else {
            usageElement.contentType = InstagramContentType.UNKNOWN
            adDataObj.contentType = InstagramContentType.UNKNOWN
        }
        return Pair(usageElement, adDataObj)
    }

    private fun pushToQueue(usageElement: InstagramUsageQueueData, adsDataElement: InstagramAdsData, context: Context) {
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


        if (adsDataElement.adCompany != null &&
            adsDataElement.adCompany!!.isNotEmpty() &&
            adsDataElement.adCompany!!.isNotBlank()) {

            if (this.instaAdsQueue.size <= 0) {
                this.instaAdsQueue.addLast(adsDataElement)
            }
            else {
                if (!(instaAdsQueue.last().adCompany.equals(adsDataElement.adCompany, true)) ||
                    !(instaAdsQueue.last().contentType.equals(adsDataElement.contentType, true))) {
                    this.instaAdsQueue.addLast(adsDataElement)
                }
                else {
                    if (instaAdsQueue.last().adCompany.equals(adsDataElement.adCompany, true)) {
                        if (adsDataElement.adDescription != null &&
                            (instaAdsQueue.last().adDescription == null ||
                                    instaAdsQueue.last().adDescription.isNullOrBlank())) {
                            instaAdsQueue.last().adDescription = adsDataElement.adDescription
                        }
                        if (adsDataElement.adDescription2 != null &&
                            (instaAdsQueue.last().adDescription2 == null ||
                                    instaAdsQueue.last().adDescription2.isNullOrBlank())) {
                            instaAdsQueue.last().adDescription2 = adsDataElement.adDescription2
                        }
                    }


                }
            }
        }
    }


    fun writeUsageDataToFile(context: Context) {
        this.writeContentData()
        this.writeAdsData()
        this.sendDataToServer(context)
    }

    private fun writeContentData() {
        val instaUsageList: ArrayList<InstagramUsageQueueData> = ArrayList()
        while (this.instaUsageQueue.size > 0) {
            val instaUsageElement: InstagramUsageQueueData = this.instaUsageQueue.first()
            if (instaUsageElement.endTime == null) {
                instaUsageElement.endTime = MiscUtils.dateFormat.format(Date())
            }
            instaUsageList.add(instaUsageElement)
            this.instaUsageQueue.removeFirst()

        }
        val dbHandler = InstagramDataDbHandler(LifeCycleActivity.context!!).getInstance(LifeCycleActivity.context!!)
        dbHandler!!.addMultipleInstagramData(instaUsageList)
    }

    private fun writeAdsData() {

        if(this.instaAdsQueue.isNotEmpty()) {

            val instaAdsList: ArrayList<InstagramAdsData> = this.instaAdsQueue.toList() as ArrayList<InstagramAdsData>
            val dbHandler = InstagramAdsDbHandler(LifeCycleActivity.context!!).getInstance(LifeCycleActivity.context!!)
            dbHandler!!.addMultipleAdData(instaAdsList)
        }
    }

    private fun sendDataToServer(context: Context) {
        val instagramTrackerContext = this;

        val dbHelper = InstagramDataDbHandler(LifeCycleActivity.context!!).getInstance(LifeCycleActivity.context!!)
        val instagramDataList: ArrayList<InstagramUsageQueueData> = dbHelper!!.viewInstagramData()

        val adsDbHelper = InstagramAdsDbHandler(LifeCycleActivity.context!!).getInstance(LifeCycleActivity.context!!)
        val instagramAdsList: ArrayList<InstagramAdsData> = adsDbHelper!!.viewInstagramAdsData()

        val entity: StringEntity =
            instagramTrackerContext.createPostRequestBody(context, instagramDataList, instagramAdsList)

        HttpUtils.post(context, ApiUrlConstants.addInstagramDataToServer, entity, "application/json",
            object : AsyncHttpResponseHandler(true) {

                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                    try {
                        val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                        if (serverResp.get("success") == true) {
                            dbHelper.deleteMultipleAppData(instagramDataList)
                            adsDbHelper.deleteMultipleAppData(instagramAdsList)
                        }
                        else {
                            Log.d("Error", serverResp.get("message").toString())
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

    private fun createPostRequestBody(context: Context, instaDataList: ArrayList<InstagramUsageQueueData>,
            instagramAdsList: ArrayList<InstagramAdsData>): StringEntity {

        val requestObj = JSONObject()
        requestObj.put("appName", "instagram")
        requestObj.put("memberId", SharedPreferencesUtils.getMemberId(context))

        val usageDataArray = JSONArray()

        instaDataList.forEach { data ->
            val usageObj = JSONObject()
            usageObj.put("contentType", data.contentType)
            usageObj.put("day", data.dayOfWeek)
            usageObj.put("endTime", data.endTime)
            usageObj.put("startTime", data.startTime)
            usageDataArray.put(usageObj)
        }
        requestObj.put("usageData", usageDataArray)

        val adsArray = JSONArray()

        instagramAdsList.forEach { data ->
            val usageObj = JSONObject()
            usageObj.put("contentType", data.contentType)
            usageObj.put("adCompany", data.adCompany)
            usageObj.put("adDescription", data.adDescription)
            usageObj.put("adDescription2", data.adDescription2)
            usageObj.put("time", data.time)
            adsArray.put(usageObj)
        }
        requestObj.put("adsData", adsArray)

        return StringEntity(requestObj.toString(), "UTF-8")

    }

    fun onDestroy(context: Context) {
        this.writeUsageDataToFile(context)
    }
}