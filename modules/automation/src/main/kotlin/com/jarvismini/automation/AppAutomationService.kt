// ===== FILE: app/src/main/java/com/jarvismini/automation/AppAutomationService.kt =====
package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AppAutomationService : AccessibilityService() {

    private val TAG = "JARVIS"
    private val WHATSAPP = "com.whatsapp"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this, "Jarvis connected", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "SERVICE CONNECTED")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 🔒 Notification only
        if (event.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return
        if (event.packageName?.toString() != WHATSAPP) return

        val data = event.parcelableData
        if (data !is Notification) return

        // 🚀 HARD OPEN WhatsApp (reliable)
        val launchIntent = packageManager.getLaunchIntentForPackage(WHATSAPP)
            ?: return

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(launchIntent)

        Toast.makeText(
            this,
            "WhatsApp opened from notification",
            Toast.LENGTH_SHORT
        ).show()

        Log.e(TAG, "WHATSAPP LAUNCHED")
    }

    override fun onInterrupt() {
        Log.e(TAG, "SERVICE INTERRUPTED")
    }
}
