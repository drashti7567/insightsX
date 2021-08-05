package com.example.diceroller.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.diceroller.R
import com.example.diceroller.constants.FileNameConstants
import com.example.diceroller.models.AppUsageQueueData
import com.example.diceroller.utils.FileUtils
import com.example.diceroller.utils.MiscUtils
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class UsagePatternActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usage_pattern)
        this.calculateTotalTimeOfEachApp()
    }

    private fun calculateTotalTimeOfEachApp() {
        val appUsageFileDataList: MutableList<String> =
            FileUtils.getFileContentAsList(this, FileNameConstants.APP_USAGE_FILE_NAME)

        val appUsageList = this.parseCsvData(appUsageFileDataList)

        val sumOfAppUsageList = this.getSumOfTotalAppUsage(appUsageList);

        val usagePatternList = findViewById<ListView>(R.id.usage_pattern_list)
        val arr: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.support_simple_spinner_dropdown_item, sumOfAppUsageList)
        usagePatternList.adapter = arr
    }

    private fun parseCsvData(appUsageFileDataList: MutableList<String>): ArrayList<AppUsageQueueData> {
        val appUsageList: ArrayList<AppUsageQueueData> = ArrayList()
        appUsageFileDataList.forEach {
            val splitData = it.split(",")
            val queueElement = AppUsageQueueData(splitData[1], splitData[0], splitData[2], splitData[3], splitData[4])
            appUsageList.add(queueElement)
        }
        return appUsageList

    }

    private fun getSumOfTotalAppUsage(appUsageList: ArrayList<AppUsageQueueData>): ArrayList<String> {
        val mapOfTotalAppTime: TreeMap<String, Long> = TreeMap()
        appUsageList.forEach {
            val appName = it.appName
            val startTime = MiscUtils.dateFormat.parse(it.startTime)
            val stopTime = MiscUtils.dateFormat.parse(it.endTime)
            val difference = stopTime.time - startTime.time

            mapOfTotalAppTime[appName] =
                if (mapOfTotalAppTime.contains(appName)) mapOfTotalAppTime[appName] ?: 0 + difference else difference
        }

        val sumTimeArrayList = ArrayList<String>()

        mapOfTotalAppTime
            .toList().sortedBy { (key, value) -> value }
            .reversed()
            .toMap()
            .forEach { (key, value) ->
                run {
                    sumTimeArrayList.add(key + " (" + value / 1000 + " secs) ")
                }
            }

        return sumTimeArrayList
    }
}