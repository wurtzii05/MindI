package se.mindi.services

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import se.mindi.utils.STT
import se.mindi.parser.AccessibilityEventUIParser
import android.content.pm.ServiceInfo
import android.view.accessibility.AccessibilityNodeInfo
import se.mindi.utils.AI
import kotlinx.coroutines.*
import se.mindi.parser.AICommandParser
import se.mindi.runner.AICommandRunner


class AccessibilityService : AccessibilityService() {

    companion object{
        private var inProgress = false
        lateinit var instance: se.mindi.services.AccessibilityService
    }
    private lateinit var stt: STT
    private var ai = AI()
    //needed for dealing with coroutines
    private val scope = MainScope()

    override fun onInterrupt() {
        Log.d("STARTUP", "STARTUPP")

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        stt = STT(this)
    }

    fun getActiveRoot(): AccessibilityNodeInfo?  {

        return rootInActiveWindow
    }

    override fun onServiceConnected() {
        instance = this
        stt = STT(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // If flag is already raised, do nothing.
        Log.d("in progress", "in progress ${event.action}, ${event.eventType} content changes ${event.contentDescription} ${event.contentChangeTypes}")
        if (inProgress) return

        //ignore rapid-fire events
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return

        val source = event.source ?: return

        inProgress = true
        // Get the source node of the event.
        Log.d("called", "called")
        event.source?.apply {

            // Use the event and node information to determine what action to
            // take.

            // Act on behalf of the user.
                try {

                    //send the notification
                    Log.d("running", "running")
                    val uiParser = AccessibilityEventUIParser.parse(rootInActiveWindow)
//                    startVoiceForeground()
//                    stt.startListening { text ->
//                        if (text!=null) {
//                            val uiParser = AccessibilityEventUIParser.parse(this)
//                            Log.d("EXPLORER", "$uiParser")
//                            scope.launch {
//                                try{
//                                    var response = ai.getAIResponse(text, uiParser.toString())
//                                    if (response != null) {
//                                        handle(response, uiParser)
//                                    }
//                                } finally{
//                                    inProgress = false
//                                    //delete the notification
//                                    stopForeground(STOP_FOREGROUND_REMOVE)
//                                }
//
//                            }
//                        //if there was no text
//                        } else{
//                            //delete the notification
//                            stopForeground(STOP_FOREGROUND_REMOVE)
//                        }
//                    }

                } catch (ex: Exception) {
                    Log.d("ERROR", "$ex")
                }
        }
        inProgress = false
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

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        Log.d("Acess", "listening")
        return false
    }

    protected override fun onKeyEvent(event: KeyEvent): Boolean {

        Log.d("keycode", "$event.keyCode")
        return super.onKeyEvent(event)
    }
    //This helps to ensure that android does not block the program from running in background
    //by allowing us to create a notification
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "voice_input_channel",
                "Voice Input Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    private fun startVoiceForeground() {
        val notification = NotificationCompat.Builder(this, "voice_input_channel")
            .setContentTitle("Voice Input Active")
            .setContentText("Listening for speech...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Ensure you have this icon
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ specific requirement
            startForeground(
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(1001, notification)
        }
    }
}