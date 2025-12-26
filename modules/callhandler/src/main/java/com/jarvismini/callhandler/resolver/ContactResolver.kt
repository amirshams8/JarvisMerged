package com.jarvismini.callhandler.resolver

import android.content.Context
import android.provider.ContactsContract

object ContactResolver {

    fun isContact(context: Context, phone: String): Boolean {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI
        val cursor = context.contentResolver.query(
            uri.buildUpon().appendPath(phone).build(),
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        )

        cursor?.use {
            return it.moveToFirst()
        }
        return false
    }
}
