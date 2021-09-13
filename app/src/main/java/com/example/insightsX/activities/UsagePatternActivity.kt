package com.example.insightsX.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.insightsX.R
import com.example.insightsX.constants.FileNameConstants
import com.example.insightsX.database.AppDataDBHandler
import com.example.insightsX.models.AppUsageQueueData
import com.example.insightsX.models.SystemLogsData
import com.example.insightsX.utils.FileUtils
import com.example.insightsX.utils.MiscUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class UsagePatternActivity : BaseActivity() {

    private fun convertMapToList(mapOfTotalAppTime: HashMap<String, Long>): ArrayList<String> {
        val sumTimeArrayList = ArrayList<String>()
        mapOfTotalAppTime
            .toList().sortedBy { (key, value) -> value }
            .reversed()
            .toMap()
            .forEach { (key, value) ->
                run {
                    sumTimeArrayList.add(key + " (" + value / 60000 + " mins) ")
                }
            }

        return sumTimeArrayList
    }

    private fun getSumOfTotalAppUsage(appUsageList: ArrayList<AppUsageQueueData>): HashMap<String, Long> {
        val mapOfTotalAppTime: HashMap<String, Long> = HashMap()
        appUsageList.forEach {
            val appName = it.appName
            val startTime = MiscUtils.dateFormat.parse(it.startTime)
            val stopTime = MiscUtils.dateFormat.parse(it.endTime)
            val difference = stopTime.time - startTime.time

            mapOfTotalAppTime[appName] =
                if (mapOfTotalAppTime.contains(appName)) (mapOfTotalAppTime[appName] ?: 0) + difference else difference
        }
        return mapOfTotalAppTime

    }

    private fun calculateTotalTimeOfEachApp() {

        val appDbHelper = AppDataDBHandler(this)
        val appUsageList = appDbHelper.viewAppData()

        val mapOfAppAndUsageTime = this.getSumOfTotalAppUsage(appUsageList);

        val sumOfAppUsageList = this.convertMapToList(mapOfAppAndUsageTime)

        val usagePatternList = findViewById<ListView>(R.id.usage_pattern_list)

        val arr: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.support_simple_spinner_dropdown_item, sumOfAppUsageList)
        usagePatternList.adapter = arr
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usage_pattern)
        this.calculateTotalTimeOfEachApp()
    }








}