package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import android.widget.Toast

class AppAutomationService : AccessibilityService() {

    private val TARGET_PACKAGE = "com.whatsapp"
    private var lastMessage: String? = null

    override fun onServiceConnected() {
        Toast.makeText(
            applicationContext,
            "JARVIS CONNECTED",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg != TARGET_PACKAGE) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val root = rootInActiveWindow ?: return

        val message = findLatestMessage(root) ?: return

        if (message == lastMessage) return
        lastMessage = message

        Log.e("JARVIS_PROOF", "📩 NEW MESSAGE: $message")

        Toast.makeText(
            applicationContext,
            "NEW MESSAGE RECEIVED",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun findLatestMessage(node: AccessibilityNodeInfo): String? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(node)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            if (current.className == "android.widget.TextView") {
                val text = current.text?.toString()
                if (!text.isNullOrBlank() && text.length < 300) {
                    return text
                }
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    override fun onInterrupt() {}
}
