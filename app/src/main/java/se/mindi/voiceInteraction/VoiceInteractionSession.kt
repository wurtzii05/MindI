package se.mindi.voiceInteraction

import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import se.mindi.parser.AICommandParser
import se.mindi.runner.AICommandRunner
import se.mindi.parser.AccessibilityEventUIParser
import se.mindi.services.AccessibilityService
import se.mindi.utils.AI
import se.mindi.utils.STT

class VoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    private lateinit var stt: STT
    private var ai = AI()
    //needed for dealing with coroutines
    private val scope = MainScope()
    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)

        try {
            // todo listen to stt

        } catch (ex: Exception) {
            Log.d("ERROR", "$ex")
        }
        hide()
    }

    override fun onHide() {
        super.onHide()
        val root = AccessibilityService.instance.getActiveRoot() ?: return
        val uiParser = AccessibilityEventUIParser.parse(root)
        val text = "open mindi"
        if (text != null) {
            scope.launch {
                var response = ai.getAIResponse(text, uiParser.toString())
                if (response != null) {
                    handle(response, uiParser)
                }
            }
        }
    }

    private fun handle(aiInput : String, explorer: AccessibilityEventUIParser)
    {
        val commands = AICommandParser.parse(aiInput)
        if (commands != null) {
            AICommandRunner.run(commands, explorer)
        } else {
            Log.e("Error", "there were no commands to run")
            // TTS.speakError("")
        }
    }
/*
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "voice_input_channel",
                "Voice Input Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(context, NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
*/
/*
    private fun startVoiceForeground() {
        val notification = NotificationCompat.Builder(context, "voice_input_channel")
            .setContentTitle("Voice Input Active")
            .setContentText("Listening for speech...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Ensure you have this icon
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ specific requirement
            context.startForeground(
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            context.startForeground(1001, notification)
        }
    }
*/
}