package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class AppAutomationService : AccessibilityService() {

    private val TARGET_PACKAGE = "com.whatsapp"
    private var lastMessage: String? = null

    override fun onServiceConnected() {
        Toast.makeText(this, "Jarvis connected", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() != TARGET_PACKAGE) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val root = rootInActiveWindow ?: return
        val message = extractIncomingMessage(root) ?: return

        if (message == lastMessage) return
        lastMessage = message

        Toast.makeText(this, "New WhatsApp message", Toast.LENGTH_SHORT).show()

        sendReply(root)
        performGlobalAction(GLOBAL_ACTION_BACK)

        Toast.makeText(this, "Jarvis replied & closed chat", Toast.LENGTH_SHORT).show()
    }

    private fun extractIncomingMessage(root: AccessibilityNodeInfo): String? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.className == "android.widget.TextView") {
                val text = node.text?.toString()
                if (!text.isNullOrBlank() && text.length < 300) {
                    return text
                }
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun sendReply(root: AccessibilityNodeInfo) {
        val input = NodeFinder.findInputField(root) ?: return
        val send = NodeFinder.findSendButton(root) ?: return

        input.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            android.os.Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    "Hello Mr Shams, Jarvis here. I’ve received your message."
                )
            }
        )

        send.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    override fun onInterrupt() {}
}
