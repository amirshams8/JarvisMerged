// ===== FILE: app/src/main/java/com/jarvismini/automation/WhatsAppNotificationListener.kt =====
package com.jarvismini.automation

import android.app.Notification
import android.app.PendingIntent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput

class WhatsAppNotificationListener : NotificationListenerService() {

    private val TAG = "JARVIS-PHASE3"
    private val WHATSAPP = "com.whatsapp"

    private var lastMessageHash: Int? = null

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.packageName != WHATSAPP) return

        val notification = sbn.notification
        val extras = notification.extras ?: return

        // Extract message text
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: return

        val hash = text.hashCode()
        if (hash == lastMessageHash) return
        lastMessageHash = hash

        Log.e(TAG, "NEW MESSAGE: $text")
        Toast.makeText(this, "Jarvis detected message", Toast.LENGTH_SHORT).show()

        // Find reply action
        val action = notification.actions?.firstOrNull { action ->
            action.remoteInputs != null
        } ?: return

        val remoteInput = action.remoteInputs.firstOrNull() ?: return
        val replyIntent = action.actionIntent ?: return

        sendReply(replyIntent, remoteInput)
    }

    private fun sendReply(
        pendingIntent: PendingIntent,
        remoteInput: RemoteInput
    ) {
        val replyText = "Hello! Jarvis here 🤖"

        val bundle = Bundle()
        bundle.putCharSequence(remoteInput.resultKey, replyText)

        val intent = android.content.Intent()
        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)

        try {
            pendingIntent.send(this, 0, intent)
            Log.e(TAG, "AUTO-REPLY SENT")
            Toast.makeText(this, "Jarvis replied", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "FAILED TO SEND REPLY", e)
        }
    }
}
