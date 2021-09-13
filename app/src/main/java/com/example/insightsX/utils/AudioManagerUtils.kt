package com.example.insightsX.utils

import android.content.Context
import android.media.AudioManager

object AudioManagerUtils {

    fun checkIfCallIsActive(context: Context): Boolean {
            val manager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
            return manager.mode == AudioManager.MODE_IN_CALL;
    }
}