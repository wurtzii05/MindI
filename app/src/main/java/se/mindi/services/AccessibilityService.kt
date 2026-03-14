package se.mindi.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast


class AccessibilityService : AccessibilityService() {
    override fun onInterrupt() {}
    override fun onServiceConnected() {
     
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Get the source node of the event
    }

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        return false
    }

    protected override fun onKeyEvent(event: KeyEvent): Boolean {

        return super.onKeyEvent(event)
    }
}