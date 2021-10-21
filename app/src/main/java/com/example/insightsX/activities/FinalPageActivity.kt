package com.example.insightsX.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.example.insightsX.R
import com.example.insightsX.constants.ApiUrlConstants
import com.example.insightsX.models.InstalledAppsData
import com.example.insightsX.models.YoutubeUsageQueueData
import com.example.insightsX.utils.HttpUtils
import com.example.insightsX.utils.InstalledAppsUtils
import com.example.insightsX.utils.SharedPreferencesUtils
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class FinalPageActivity : BaseActivity() {

    private var buttonCount = 0
    private var lastVolumeButtonClickedTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.final_app_page)
        this.uploadInstalledApps()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        /**
         * Shortcut function which on click on volume down button 8 times will show track activity page
         */
        val time = Date().time
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (this.lastVolumeButtonClickedTime == null || (time - this.lastVolumeButtonClickedTime!!) < 1000) {
                this.buttonCount++
            }
            else {
                this.buttonCount = 0
            }
        }
        if (buttonCount == 8) {
            buttonCount = 0
            val trackingAppDataActivity: Intent = Intent(this, TrackingAppDataActivity::class.java)
            startActivity(trackingAppDataActivity)
        }

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            super.onBackPressed()
        }
        return true;
    }

    private fun uploadInstalledApps() {
        /**
         * Function to upload the installed apps of the member's phone to server db
         */
        if(SharedPreferencesUtils.getInstalledAppsUploaded(this) != null &&
                    SharedPreferencesUtils.getInstalledAppsUploaded(this) == false) {

            val installedAppsList: ArrayList<InstalledAppsData> =
                InstalledAppsUtils.getInstalledApps(this.packageManager);
            val entity: StringEntity = this.createPostRequestBody(this, installedAppsList);
            val context = this

            HttpUtils.post(this, ApiUrlConstants.addInstalledAppsToDb, entity, "application/json",
                object: AsyncHttpResponseHandler(true) {

                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        try {
                            val serverResp = JSONObject(String(responseBody!!, Charsets.UTF_8))
                            if (serverResp.get("success") == true) {
                                SharedPreferencesUtils.setInstalledAppsUploaded(context, true);
                            }
                            else {
                                Log.d("Error", serverResp.get("message").toString())
                            }
                        }
                        catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    override fun onFailure(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?, error: Throwable?) {
                        Log.d("Error", error.toString() + " " + responseBody.toString())
                    }
                })
        }
    }

    private fun createPostRequestBody(context: Context,
                                      installedAppsList: ArrayList<InstalledAppsData>): StringEntity {
        /**
         * Function to create String entity - json object for the post request
         * to upload the installed apps list to server.
         */
        val requestObj = JSONObject()
        requestObj.put("memberId", SharedPreferencesUtils.getMemberId(context))

        val installedAppsArray = JSONArray()

        installedAppsList.forEach { data ->
            val usageObj = JSONObject()
            usageObj.put("appName", data.appName)
            usageObj.put("isSystemApp", data.isSystemPackage)
            usageObj.put("packageName", data.packageName)
            installedAppsArray.put(usageObj)
        }
        requestObj.put("installedAppsList", installedAppsArray)

        return StringEntity(requestObj.toString(), "UTF-8")
    }
}