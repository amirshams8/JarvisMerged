package com.jarvismini.callhandler.sms

import android.telephony.SmsManager
import android.util.Log
import com.jarvismini.callhandler.CallConstants

object CallAutoReply {

    fun send(phone: String) {
        try {
            SmsManager.getDefault().sendTextMessage(
                phone,
                null,
                CallConstants.AUTO_REPLY_TEXT,
                null,
                null
            )
            Log.d(CallConstants.TAG, "Auto-reply SMS sent to $phone")
        } catch (e: Exception) {
            Log.e(CallConstants.TAG, "Failed to send SMS", e)
        }
    }
}
