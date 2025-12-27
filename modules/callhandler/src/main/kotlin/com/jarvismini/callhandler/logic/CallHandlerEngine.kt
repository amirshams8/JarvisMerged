package com.jarvismini.callhandler.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState
import com.jarvismini.service.CallAutoReplyService
import java.util.concurrent.ConcurrentHashMap

object CallHandlerEngine {

    private const val TAG = "CALL-HANDLER"
    private const val COOLDOWN_MS = 60_000L

    private val lastHandled = ConcurrentHashMap<String, Long>()

    fun handleIncomingCall(context: Context, number: String) {
        val now = System.currentTimeMillis()

        Log.d(TAG, "Incoming call from $number")

        // 🔕 Jarvis OFF
        if (JarvisState.currentMode == JarvisMode.NORMAL) {
            Log.d(TAG, "Jarvis NORMAL → ignoring call")
            return
        }

        // 🔒 Contacts only
        if (!isSavedContact(context, number)) {
            Log.d(TAG, "Number not in contacts → ignored")
            return
        }

        // 🔁 Cooldown
        val last = lastHandled[number] ?: 0L
        if (now - last < COOLDOWN_MS) {
            Log.d(TAG, "Cooldown active → ignored")
            return
        }
        lastHandled[number] = now

        // 🧠 Decision
        val decision = AutoReplyOrchestrator.handle(
            AutoReplyInput(
                messageText = "Incoming call",
                isFromOwner = false
            )
        )

        if (decision !is ReplyDecision.AutoReply) {
            Log.d(TAG, "No auto-reply decision")
            return
        }

        Log.d(TAG, "Auto-reply decided → starting Foreground Service")

        val intent = Intent(
            context.applicationContext,
            CallAutoReplyService::class.java
        ).apply {
            putExtra(CallAutoReplyService.EXTRA_NUMBER, number)
            putExtra(CallAutoReplyService.EXTRA_MESSAGE, decision.message)
        }

        // ✅ Android 8+ required
        context.applicationContext.startForegroundService(intent)
    }

    private fun isSavedContact(context: Context, number: String): Boolean {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            .buildUpon()
            .appendPath(Uri.encode(number))
            .build()

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        )

        return cursor?.use { it.moveToFirst() } == true
    }
}
