package se.mindi.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityService : AccessibilityService() {
    override fun onInterrupt() {}
    override fun onServiceConnected() {
     
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Get the source node of the event
        if (event.eventType == 1) {
            val source = event.source?.parent ?: return
            val uiHierarchy = getUIString(source)
            Log.d("ACCESS", uiHierarchy)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        return false
    }
    protected override fun onKeyEvent(event: KeyEvent): Boolean {
        return super.onKeyEvent(event)
    }

    fun getUIString(node: AccessibilityNodeInfo, state: String = ""): String {
        var s = state
        for (i in 0 until node.childCount) {
            try {
                val child = node.getChild(i)
                s = getUIString(child, s)
                if (child.isClickable) {
                    s += "{CLASS: ${child.className}, BUTTON: ${child.text}}\n"
                } else {
                    s += "{CLASS: ${child.className}, TEXT: ${child.text}}\n"
                }
            } catch (ex: Exception) {
                Log.d("ERROR", "$ex")
                return ""
            }
        }
        return "$s"
    }
}