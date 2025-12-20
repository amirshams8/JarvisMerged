package com.jarvismini.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.core.JarvisState

class AppAutomationService : AccessibilityService() {

    private val TAG = "JarvisMini-AutoService"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Automation Service connected")
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            canRetrieveWindowContent = true
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                // Notification arrived, try to open chat
                event.parcelableData?.let { notif ->
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
                    senderName = senderName,
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