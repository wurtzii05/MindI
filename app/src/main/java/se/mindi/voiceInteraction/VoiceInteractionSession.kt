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
import se.mindi.extensions.toUINode
import se.mindi.model.AICommandType
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
        // kill any previous tasks
        AccessibilityService.isAiTaskRunning = false
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
            //val text = "open mcdonalds and add a burger to the cart"

            stt.startListening { text ->
                if (text != null) {
                    storedSpeech = text
                    Log.d("Strings", storedSpeech)


                    getAIResponse(text)


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
    fun getAIResponse(speech: String)
    {

        Log.d("STAMP","ONHIDE")
        scope.launch {
            while (true) {
                val root = AccessibilityService.instance.getActiveRoot()?.toUINode() ?: break
                val uiParser = AccessibilityEventUIParser.parse(root)
                Log.d("UI", uiParser.toString())
                var response = ai.getAIResponse(storedSpeech,uiParser.toString())
                Log.d("response", "$response")
                //finishSession()
                if (response != null) {
                    val isFinished = ai.handleAiCommand(response, uiParser)
                    if (isFinished) {
                        break
                    } else {
                        storedSpeech = ""
                        Thread.sleep(2000)
                    }
                }
            }
        }
    }




}