package com.example.insightsX.constants

class ForegroundServiceConstants {
    interface ACTION {
        companion object {
            const val MAIN_ACTION = "com.example.insightsX.action.main"
            const val PREV_ACTION = "com.example.insightsX.action.prev"
            const val PLAY_ACTION = "com.example.insightsX.action.play"
            const val NEXT_ACTION = "com.example.insightsX.action.next"
            const val STARTFOREGROUND_ACTION = "com.example.insightsX.action.startforeground"
            const val STOPFOREGROUND_ACTION = "com.example.insightsX.action.stopforeground"
        }
    }

    interface NOTIFICATION_ID {
        companion object {
            const val FOREGROUND_SERVICE = 101
        }
    }
}