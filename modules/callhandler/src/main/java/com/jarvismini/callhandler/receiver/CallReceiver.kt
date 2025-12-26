package com.jarvismini.callhandler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.jarvismini.callhandler.CallConstants
import com.jarvismini.callhandler.resolver.ContactResolver
import com.jarvismini.callhandler.sms.CallAutoReply

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state != TelephonyManager.EXTRA_STATE_RINGING) return

        val number =
            intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                ?: return

        Log.d(CallConstants.TAG, "Incoming call from $number")

        if (!ContactResolver.isContact(context, number)) {
            Log.d(CallConstants.TAG, "Ignored (not a contact)")
            return
        }

        CallAutoReply.send(number)
    }
}
