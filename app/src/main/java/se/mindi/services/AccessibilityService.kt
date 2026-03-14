package se.mindi.services

import android.accessibilityservice.AccessibilityGestureEvent
import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.annotation.RequiresApi


class AccessibilityService : AccessibilityService() {
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Get the source node of the event.
        event.source?.apply {

            // Use the event and node information to determine what action to
            // take.

            // Act on behalf of the user.
            performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
        Log.d("ACCESS", "yo we got something")
    }

    @Deprecated("Deprecated in Java")
    override fun onGesture(event: Int): Boolean {
        Log.d("Acess", "listening")
        return false
    }

    protected override fun onKeyEvent(event: KeyEvent): Boolean {
        val action: Int = event.getAction()
        val keyCode: Int = event.getKeyCode()
        // the service listens for both pressing and releasing the key
        // so the below code executes twice, i.e. you would encounter two Toasts
        // in order to avoid this, we wrap the code inside an if statement
        // which executes only when the key is released
        if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                Log.d("Check", "KeyUp")
                Toast.makeText(this, "KeyUp", Toast.LENGTH_SHORT).show()
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                Log.d("Check", "KeyDown")
                Toast.makeText(this, "KeyDown", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onKeyEvent(event)
    }
}