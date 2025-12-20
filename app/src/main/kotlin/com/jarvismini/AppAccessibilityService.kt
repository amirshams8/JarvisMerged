package com.jarvismini

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.automation.decision.ReplyDecision

class AppAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("JarvisService", "Accessibility Service CONNECTED")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        Log.d(
            "JarvisService",
            "Event: ${event.eventType} | Package: ${event.packageName}"
        )

        // TEMP: Only observe WhatsApp
        if (event.packageName != "com.whatsapp") return

        val text = event.text?.joinToString(" ") ?: return
        if (text.isBlank()) return

        val decision = AutoReplyOrchestrator.handle(
            AutoReplyInput(
                messageText = text,
                isFromOwner = false
            )
        )

        when (decision) {
            is ReplyDecision.AutoReply ->
                Log.i("JarvisService", "AutoReply decided: ${decision.message}")

            ReplyDecision.NoReply ->
                Log.i("JarvisService", "No reply decision")
        }
    }

    override fun onInterrupt() {
        Log.w("JarvisService", "Accessibility Service INTERRUPTED")
    }
}
