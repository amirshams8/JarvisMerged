// ===== FILE: modules/callhandler/src/main/java/com/jarvismini/callhandler/JarvisCallScreeningService.kt =====
package com.jarvismini.callhandler

import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.SmsManager
import android.util.Log
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState
import java.util.concurrent.ConcurrentHashMap

class JarvisCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "CALL-HANDLER"
        private const val COOLDOWN_MS = 60_000L // 1 min per number
    }

    private val lastHandled = ConcurrentHashMap<String, Long>()

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle ?: return
        val number = handle.schemeSpecificPart ?: return
        val now = System.currentTimeMillis()

        // ✅ Always respond — required by CallScreeningService
        fun allow() {
            respondToCall(
                callDetails,
                CallResponse.Builder().build()
            )
        }

        // 🧠 JARVIS OFF → allow ringing
        if (JarvisState.currentMode == JarvisMode.OFF) {
            Log.d(TAG, "Jarvis OFF → allowing call")
            allow()
            return
        }

        // 🔒 CONTACT-ONLY enforcement
        if (!isSavedContact(number)) {
            Log.d(TAG, "Unknown number → allowing call: $number")
            allow()
            return
        }

        // 🔁 Cooldown protection
        val last = lastHandled[number] ?: 0L
        if (now - last < COOLDOWN_MS) {
            Log.d(TAG, "Cooldown active → allowing call: $number")
            allow()
            return
        }
        lastHandled[number] = now

        // 🧠 Ask orchestrator
        val decision = AutoReplyOrchestrator.handle(
            AutoReplyInput(
                messageText = "Incoming call",
                isFromOwner = false
            )
        )

        if (decision !is ReplyDecision.AutoReply) {
            Log.d(TAG, "Orchestrator blocked auto-reply")
            allow()
            return
        }

        // 📩 Send SMS
        sendSms(number, decision.message)

        // 🔕 Silence + reject call
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build()
        )

        Log.d(TAG, "Call silenced + SMS sent → $number")
    }

    // ================= HELPERS =================

    private fun isSavedContact(number: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )

        val cursor = contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        )

        return cursor?.use { it.moveToFirst() } == true
    }

    private fun sendSms(number: String, message: String) {
        try {
            SmsManager.getDefault()
                .sendTextMessage(number, null, message, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "SMS send failed", e)
        }
    }
}
