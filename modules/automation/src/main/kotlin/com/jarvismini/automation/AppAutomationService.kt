package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper

class AppAutomationService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Listen only to notification changes
        if (event.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return

        val text = event.text?.joinToString("") ?: return

        // Trigger only on WhatsApp
        if (!text.contains("WhatsApp", ignoreCase = true)) return

        handler.postDelayed({
            replyToWhatsApp()
        }, 1500)
    }

    private fun replyToWhatsApp() {
        val root = rootInActiveWindow ?: return

        val inputFields = root.findAccessibilityNodeInfosByViewId(
            "com.whatsapp:id/entry"
        )

        if (inputFields.isEmpty()) return

        val input = inputFields[0]
        input.performAction(AccessibilityNodeInfo.ACTION_FOCUS)

        val args = android.os.Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            "This is Jarvis, assisting Mr Shams. He will respond shortly."
        )
        input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        handler.postDelayed({
            val sendButtons = root.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/send"
            )
            if (sendButtons.isNotEmpty()) {
                sendButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }, 500)
    }

    override fun onInterrupt() {
        // No-op
    }
}
