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
import se.mindi.utils.UIHierarchyExplorer
import android.app.Service
import android.content.pm.ServiceInfo
import se.mindi.R
import se.mindi.utils.AI
import kotlinx.coroutines.*
import se.mindi.utils.TTS


class AccessibilityService : AccessibilityService() {

    companion object{
        private var inProgress = false
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

    override fun onServiceConnected() {
        Log.d("STARTUP", "STARTUP")
        stt = STT(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // If flag is already raised, do nothing.
        if (inProgress) return

        //ignore rapid-fire events
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return

        val source = event.source ?: return

        inProgress = true
        // Get the source node of the event.
        event.source?.apply {

            // Use the event and node information to determine what action to
            // take.

            // Act on behalf of the user.
                try {

                    //send the notification
                    startVoiceForeground()
                    stt.startListening { text ->
                        if (text!=null) {
                            val explorer = UIHierarchyExplorer.parse(this)
                            Log.d("EXPLORER", "$explorer")
                            scope.launch {
                                try{
                                    var response = ai.getAIResponse(text, explorer.toString())
                                    if (response != null) {
                                        handle(response)

                                    }
                                } finally{
                                    inProgress = false
                                    //delete the notification
                                    stopForeground(STOP_FOREGROUND_REMOVE)
                                }

                            }
                        //if there was no text
                        } else{
                            inProgress = false
                            //delete the notification
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        }
                    }

                } catch (ex: Exception) {
                    Log.d("ERROR", "$ex")
                    inProgress = false
                }
        }
    }
    private fun handle(aiInput : String)
    {
        aiInput.lines().forEach { line ->
            if (line.substring(0,3).equals("Say")) TTS.Companion.speakOut(line.substring(3,line.length-1))
            //else if (line.substring(0,6).equals("Select"))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        Log.d("Acess", "listening")
        return false
    }

    protected override fun onKeyEvent(event: KeyEvent): Boolean {

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