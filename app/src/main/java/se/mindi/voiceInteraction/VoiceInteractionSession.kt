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
import android.view.WindowManager
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
    private lateinit var storedSpeech:String
    //needed for dealing with coroutines
    private val scope = MainScope()
    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)

        val window = window?.window ?: return
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )
        //showNotification()
        try {
            val text = ""
            val root = AccessibilityService.instance.getActiveRoot() ?: return
            stt.startListening { text ->
                if (text != null) {
                    storedSpeech = text?:""
                    Log.d("Strings", storedSpeech)

                    val uiParser = AccessibilityEventUIParser.parse(root)
                    getAIResponse(storedSpeech, uiParser)

                } else {
                    //finishSession()
                }
            }
        } catch (ex: Exception) {
            Log.d("ERROR", "$ex")
        }
    }

    override fun onHide()
    {
        super.onHide()
    }
    fun getAIResponse(speech: String, uiParser: AccessibilityEventUIParser)
    {
        Log.d("UI", uiParser.toString())
        Log.d("STAMP","ONHIDE")
        scope.launch {
            var response = ai.getAIResponse(storedSpeech, uiParser.toString())
            //finishSession()
            if (response != null) {
                handle(response, uiParser)
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
    private fun showNotification() {
        val channelId = "voice_input_channel"
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle("Assistant Active")
            .setContentText("Listening for commands...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }
    private fun finishSession() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Remove the notification
        manager.cancel(1)
        // Close the assistant overlay
        finish()
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

    //override fun onCreateContentView(): View {
    //    val view = LayoutInflater.from(context).inflate(R.layout.assistant_overlay, null)
    //    return view
    //}



}