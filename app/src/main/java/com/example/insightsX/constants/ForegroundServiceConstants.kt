package com.example.insightsX.constants

class ForegroundServiceConstants {
    interface ACTION {
        companion object {
            const val MAIN_ACTION = "com.truiton.foregroundservice.action.main"
            const val PREV_ACTION = "com.truiton.foregroundservice.action.prev"
            const val PLAY_ACTION = "com.truiton.foregroundservice.action.play"
            const val NEXT_ACTION = "com.truiton.foregroundservice.action.next"
            const val STARTFOREGROUND_ACTION = "com.truiton.foregroundservice.action.startforeground"
            const val STOPFOREGROUND_ACTION = "com.truiton.foregroundservice.action.stopforeground"
        }
    }

    interface NOTIFICATION_ID {
        companion object {
            const val FOREGROUND_SERVICE = 101
        }
    }
}