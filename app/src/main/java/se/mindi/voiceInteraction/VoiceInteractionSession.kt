package se.mindi.voiceInteraction

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import se.mindi.R
import se.mindi.parser.AICommandParser
import se.mindi.runner.AICommandRunner
import se.mindi.parser.AccessibilityEventUIParser
import se.mindi.services.AccessibilityService
import se.mindi.utils.AI
import se.mindi.utils.STT

class VoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    private var stt = STT(context)
    private var ai = AI()
    //needed for dealing with coroutines
    private val scope = MainScope()
    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)

        val root = AccessibilityService.instance.getActiveRoot() ?: return
        val uiParser = AccessibilityEventUIParser.parse(root)
        try {
            //send the notification
            val text = ""
             stt.startListening { text ->
                if (text != null) {
                    scope.launch {
                        var response = ai.getAIResponse(text, uiParser.toString())
                        if (response != null) {
                            handle(response, uiParser)
                        }
                    }
                }
            }

        } catch (ex: Exception) {
            Log.d("ERROR", "$ex")
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

    override fun onCreateContentView(): View {
        // Inflate a custom layout (e.g., a bottom sheet with a mic icon)
        val view = LayoutInflater.from(context).inflate(R.layout.assistant_overlay, null)
        return view
    }



}