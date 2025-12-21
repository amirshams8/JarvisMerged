package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AppAutomationService : AccessibilityService() {

    private val TAG = "JARVIS-A11Y"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e(TAG, "🔥 SERVICE CONNECTED")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        Log.e(
            TAG,
            "EVENT => type=${event.eventType}, pkg=${event.packageName}, text=${event.text}"
        )
    }

    override fun onInterrupt() {
        Log.e(TAG, "❌ SERVICE INTERRUPTED")
    }
}
