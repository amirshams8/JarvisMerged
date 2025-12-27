package com.jarvismini.callhandler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.SmsManager
import android.util.Log
import android.provider.ContactsContract
import android.net.Uri
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState

class CallStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CALL-HANDLER"
        private const val COOLDOWN_MS = 60_000L
        private var lastHandled = mutableMapOf<String, Long>()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

        if (state != TelephonyManager.EXTRA_STATE_RINGING) return

        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            ?: return

        Log.d(TAG, "Incoming call: $number")

        // Jarvis off → do nothing
        if (JarvisState.currentMode == JarvisMode.NORMAL) return

        // if caller not in contacts → ignore
        if (!isSavedContact(context, number)) return

        val now = System.currentTimeMillis()
        val lastTime = lastHandled[number] ?: 0L
        if (now - lastTime < COOLDOWN_MS) return
        lastHandled[number] = now

        val decision = AutoReplyOrchestrator.handle(
            AutoReplyInput(
                messageText = "Incoming call",
                isFromOwner = false
            )
        )

        if (decision is ReplyDecision.AutoReply) {
            SmsManager.getDefault().sendTextMessage(
                number, null, decision.message, null, null
            )
            Log.d(TAG, "SMS auto-replied for call")
        }
    }

    private fun isSavedContact(context: Context, number: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val exists = cursor?.use { it.moveToFirst() } == true
        cursor?.close()
        return exists
    }
}
