package se.mindi.services

import android.service.voice.VoiceInteractionService
import android.util.Log
import androidx.annotation.NonNull

class VoiceInteractionService: VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
        Log.d("VoiceInteractionService", "ready")
    }
}