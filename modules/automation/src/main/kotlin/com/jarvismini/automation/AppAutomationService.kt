// ===== FILE: modules/automation/src/main/kotlin/com/jarvismini/automation/AppAutomationService.kt =====
package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator

class AppAutomationService : AccessibilityService() {

    private val TAG = "JarvisMini-AutoService"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Automation Service connected")

        // IMPORTANT: do NOT reassign serviceInfo (it's a val)
        val info = AccessibilityServiceInfo()
        info.eventTypes =
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED or
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100
        info.canRetrieveWindowContent = true

        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {

            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                Log.i(TAG, "Notification detected")
                // Future: open chat via pendingIntent if needed
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val root = rootInActiveWindow ?: return

                val messageText = MessageExtractor.getLastIncomingMessage(root)
                if (messageText.isBlank()) return

                val input = AutoReplyInput(
                    messageText = messageText,
                    isFromOwner = false
                )

                val decision = AutoReplyOrchestrator.handle(input)

                if (decision is ReplyDecision.AutoReply) {
                    Log.i(TAG, "Auto replying: ${decision.message}")
                    sendMessage(root, decision.message)
                } else {
                    Log.i(TAG, "No reply decision")
                }

                AutoCloser.closeChat(root)
            }
        }
    }

    private fun sendMessage(root: AccessibilityNodeInfo, message: String) {
        val inputField = NodeFinder.findInputField(root)
        val sendButton = NodeFinder.findSendButton(root)

        if (inputField != null) {
            val args = Bundle()
            args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                message
            )
            inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    override fun onInterrupt() {
        Log.i(TAG, "Automation Service interrupted")
    }
}                event.parcelableData?.let { notif ->
                    Log.i(TAG, "Notification detected: $notif")
                    val root = rootInActiveWindow
                    ChatOpener.openChat(this, root)
                }
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val root = event.source ?: return

                val messageText = MessageExtractor.getLastIncomingMessage(root)
                val senderName = MessageExtractor.getSenderName(root)

                if (messageText.isEmpty()) return

                val input = AutoReplyInput(
                    messageText = messageText,
                    isFromOwner = false
                )

                val decision = AutoReplyOrchestrator.handle(input)

                when (decision) {            val root = event.source ?: return

            val messageText = MessageExtractor.getLastIncomingMessage(root)
            if (messageText.isEmpty()) return

            val input = AutoReplyInput(
                messageText = messageText,
                isFromOwner = false
            )

            val decision = AutoReplyOrchestrator.handle(input)

            when (decision) {
                is ReplyDecision.AutoReply -> {
                    Log.i(TAG, "Jarvis Auto-Reply: ${decision.message}")
                    sendMessage(root, decision.message)
                }
                ReplyDecision.NoReply -> {
                    Log.i(TAG, "Jarvis will not reply")
                }
            }

            // Auto-close after processing
            AutoCloser.closeChat(root)
        }
    }
}

private fun sendMessage(root: AccessibilityNodeInfo, message: String) {
    val inputField = NodeFinder.findInputField(root)
    val sendButton = NodeFinder.findSendButton(root)

    if (inputField != null) {
        val args = android.os.Bundle()
        args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message)
        inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        Log.i(TAG, "Message set in input field")
    }

    sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    Log.i(TAG, "Send button clicked")
}

override fun onInterrupt() {
    Log.i(TAG, "Automation Service interrupted")
}
}
