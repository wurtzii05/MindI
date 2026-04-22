package se.mindi.services

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log

class VoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession? {
        Log.d("VoiceInteractionSessionService", "starting new session")
        return se.mindi.voiceInteraction.VoiceInteractionSession(this)
    }
}