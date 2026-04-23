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
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import se.mindi.utils.AI
import kotlinx.coroutines.*
import se.mindi.extensions.toUINode
import se.mindi.model.UINodeType
import se.mindi.parser.AICommandParser
import se.mindi.runner.AICommandRunner


class AccessibilityService : AccessibilityService() {

    companion object{
        private var inProgress = false
        lateinit var instance: se.mindi.services.AccessibilityService
        var isAiTaskRunning = false
        var storedAISpeech = ""
        var lock = false
    }
    private lateinit var stt: STT
    private var ai = AI()
    //needed for dealing with coroutines
    private val scope = MainScope()

    private val handler = Handler(Looper.getMainLooper())
    private val uiSettledRunnable = Runnable {
        onUiSettled()
    }

    private fun onUiSettled() {
        if (!isAiTaskRunning) {
            return
        }

        if (lock) {
            return
        }
        // UI has loaded a new window/screen
        scope.launch {
            val root = se.mindi.services.AccessibilityService.instance.getActiveRoot()?.toUINode() ?: return@launch
            val uiParser = AccessibilityEventUIParser.parse(root)

            // make sure the ui has fully launched with at least one ui button
            if (!uiParser.uiNodes.any { el ->
                    el.nodeType == UINodeType.CLICKABLE
                }) {
                return@launch
            }
            Log.d("ui parser", "$uiParser")
            lock = true

            Log.d("UI", uiParser.toString())
            var response = ai.getAIResponse(se.mindi.services.AccessibilityService.storedAISpeech, uiParser.toString())
            Log.d("response", "$response")
            //finishSession()
            if (response != null) {
                val isFinished = ai.handleAiCommand(response, uiParser)
                if (isFinished) {
                    isAiTaskRunning = false
                }
            }

            lock = false
        }
    }

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
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED-> {
                // will wait for ui changes to stop for at least 500 millseconds so we know, its safe to proceed
                handler.removeCallbacks(uiSettledRunnable)
                handler.postDelayed(uiSettledRunnable, 500L)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        Log.d("Access", "listening")
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