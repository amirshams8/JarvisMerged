package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.widget.Toast

class AppAutomationService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.e("JARVIS_PROOF", "🔥 SERVICE CONNECTED")

        Toast.makeText(
            this,
            "JARVIS ACCESSIBILITY CONNECTED",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        Toast.makeText(
            this,
            "ACCESSIBILITY EVENT RECEIVED",
            Toast.LENGTH_SHORT
        ).show()

        Log.e(
            "JARVIS_PROOF",
            "EVENT TYPE=${event.eventType} PKG=${event.packageName}"
        )
    }

    override fun onInterrupt() {
        Log.e("JARVIS_PROOF", "SERVICE INTERRUPTED")
    }
}
