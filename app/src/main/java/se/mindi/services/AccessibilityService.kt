package se.mindi.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlin.math.log


class AccessibilityService : AccessibilityService() {
    override fun onInterrupt() {}
    override fun onServiceConnected() {

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Get the source node of the event
        val source = event.source ?: return
        val parent = source.parent ?: return
        val childcount = source.childCount
    recursiveChild(source)
    }

    fun recursiveChild(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            node.getChild(i)?.className?.let {
                when (it.toString()) {
                    "android.widget.TextView" -> Log.d("CHILD", node.getChild(i).toString())
                }
            }
            // Log.d("CHILD", node.getChild(i).)
            recursiveChild(node.getChild(i))

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        return false
    }

    protected override fun onKeyEvent(event: KeyEvent): Boolean {

        return super.onKeyEvent(event)
    }

    private fun getUITree() {

    }
}