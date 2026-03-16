package se.mindi.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import se.mindi.utils.UIHierarchyExplorer


class AccessibilityService : AccessibilityService() {
    override fun onInterrupt() {
        Log.d("STARTUP", "STARTUPP")

    }
    override fun onServiceConnected() {
        Log.d("STARTUP", "STARTUP")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Get the source node of the event.
        event.source?.apply {

            // Use the event and node information to determine what action to
            // take.

            // Act on behalf of the user.
                try {
                    val explorer = UIHierarchyExplorer.parse(this)
                    Log.d("EXPLORER", "$explorer")

                } catch (ex: Exception) {
                    Log.d("ERROR", "$ex")
                }
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
}