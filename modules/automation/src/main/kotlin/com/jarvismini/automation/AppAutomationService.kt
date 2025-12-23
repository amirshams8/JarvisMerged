package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.widget.Toast

class AppAutomationService : AccessibilityService() {

    private val TAG = "JARVIS_PROOF"
    private val TARGET_PACKAGE = "com.whatsapp"

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.e(TAG, "SERVICE CONNECTED")

        Toast.makeText(
            this,
            "JARVIS ACCESSIBILITY CONNECTED",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg != TARGET_PACKAGE) return

        Log.e(TAG, "WHATSAPP EVENT: ${event.eventType}")

        Toast.makeText(
            this,
            "WhatsApp event detected",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onInterrupt() {
        Log.e(TAG, "SERVICE INTERRUPTED")
    }
}
