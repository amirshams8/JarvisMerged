package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AppAutomationService : AccessibilityService() {

    private val TAG = "JarvisMini"
    private val TARGET_PACKAGE = "com.whatsapp"

    private var lastSeenMessage: String? = null

    override fun onServiceConnected() {
        Log.i(TAG, "✅ Accessibility Service CONNECTED")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return

        // 🔒 WhatsApp only
        if (pkg != TARGET_PACKAGE) return

        // 🔒 Content changes only
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val root = rootInActiveWindow ?: return

        val message = extractLatestMessage(root) ?: return

        // 🔁 Ignore duplicates
        if (message == lastSeenMessage) return
        lastSeenMessage = message

        Log.i(TAG, "📩 NEW MESSAGE: $message")

        // 🚫 Auto-reply OFF (next step)
        // sendReply(root)
    }

    private fun extractLatestMessage(root: AccessibilityNodeInfo): String? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        var latestText: String? = null

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()

            if (node.className == "android.widget.TextView") {
                val text = node.text?.toString()
                if (!text.isNullOrBlank() && text.length < 500) {
                    latestText = text
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return latestText
    }

    override fun onInterrupt() {
        Log.w(TAG, "⚠️ Accessibility Interrupted")
    }
}
