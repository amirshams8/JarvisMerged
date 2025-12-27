package com.jarvismini.callhandler.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.os.Build
import android.util.Log
import com.jarvismini.service.CallAutoReplyService   // ✅ FIXED IMPORT (APP MODULE)
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState

class CallAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        val text = event.text?.joinToString(" ") ?: return

        if (
            !text.contains("incoming", ignoreCase = true) &&
            !text.contains("calling", ignoreCase = true)
        ) return

        Log.d("CALL-A11Y", "Call UI detected: $text")

        // 🔕 Jarvis OFF
        if (JarvisState.currentMode == JarvisMode.NORMAL) {
            Log.d("CALL-A11Y", "Jarvis NORMAL → ignore")
            return
        }

        val number = extractPhoneNumber(text)
        if (number.isNullOrBlank()) {
            Log.d("CALL-A11Y", "No number detected")
            return
        }

        val decision = AutoReplyOrchestrator.handle(
            AutoReplyInput(
                messageText = "Incoming call",
                isFromOwner = false
            )
        )

        if (decision !is ReplyDecision.AutoReply) {
            Log.d("CALL-A11Y", "No auto-reply decision")
            return
        }

        val intent = Intent(
            applicationContext,
            CallAutoReplyService::class.java
        ).apply {
            putExtra(CallAutoReplyService.EXTRA_NUMBER, number)
            putExtra(CallAutoReplyService.EXTRA_MESSAGE, decision.message)
        }

        // ✅ ANDROID O+ SAFE FOREGROUND START
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(intent)
        } else {
            applicationContext.startService(intent)
        }
    }

    override fun onInterrupt() {
        // no-op
    }

    private fun extractPhoneNumber(text: String): String? {
        val regex = Regex("(\\+?\\d{10,13})")
        return regex.find(text)?.value
    }
}
