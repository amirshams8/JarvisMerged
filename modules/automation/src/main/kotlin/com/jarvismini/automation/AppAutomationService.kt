package com.jarvismini.automation
import android.accessibilityservice.AccessibilityService import android.view.accessibility.AccessibilityEvent import android.app.Notification import android.util.Log import android.widget.Toast
class AppAutomationService : AccessibilityService() {
private val TAG = "JARVIS_NOTIFY"
private val WHATSAPP_PKG = "com.whatsapp"

override fun onServiceConnected() {
    super.onServiceConnected()

    Log.e(TAG, "SERVICE CONNECTED")
    Toast.makeText(
        this,
        "Jarvis accessibility connected",
        Toast.LENGTH_LONG
    ).show()
}

override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event == null) return

    // 🔒 ONLY notification events
    if (event.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return

    // 🔒 ONLY WhatsApp
    val pkg = event.packageName?.toString() ?: return
    if (pkg != WHATSAPP_PKG) return

    val notification = event.parcelableData as? Notification ?: return
    val extras = notification.extras ?: return

    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

    if (text.isNullOrBlank()) return

    // 🚫 Ignore "You", calls, media-only, system junk
    if (
        text.contains("calling", true) ||
        text.contains("missed", true) ||
        text.contains("video", true) ||
        title == "WhatsApp"
    ) return

    Log.e(TAG, "REAL MESSAGE DETECTED → $title : $text")

    Toast.makeText(
        this,
        "New WhatsApp message:\n$text",
        Toast.LENGTH_LONG
    ).show()
}

override fun onInterrupt() {
    Log.e(TAG, "SERVICE INTERRUPTED")
}
}
