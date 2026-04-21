package se.mindi.voiceInteraction

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import se.mindi.parser.AccessibilityEventUIParser
import se.mindi.services.AccessibilityService

class VoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
    }
}