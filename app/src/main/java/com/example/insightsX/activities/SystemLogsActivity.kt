package com.example.insightsX.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.insightsX.R
import com.example.insightsX.constants.FileNameConstants
import com.example.insightsX.utils.FileUtils

class SystemLogsActivity: BaseActivity() {
    private fun parseLogs(systemLogsFileList: MutableList<String>): ArrayList<String> {
        val systemLogsList: ArrayList<String> = ArrayList()
        systemLogsFileList.forEach{
            val splitData = it.split(",")
            val logElement = splitData[0] + "\t" + splitData[1]
            systemLogsList.add(logElement)
        }
        return  systemLogsList
    }

    private fun showSystemLogs() {
        val systemLogsFileList: MutableList<String> =
            FileUtils.getFileContentAsList(this, FileNameConstants.SYSTEM_LOGS_FILE_NAME)
        val systemLogsList = this.parseLogs(systemLogsFileList)

        val systemLogsResList = findViewById<ListView>(R.id.system_logs_list)

        val arr: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.support_simple_spinner_dropdown_item, systemLogsList)
        systemLogsResList.adapter = arr
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.system_logs)
        this.showSystemLogs()
    }
}