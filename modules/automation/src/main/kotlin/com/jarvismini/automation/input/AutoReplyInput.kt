package com.jarvismini.automation.input

data class AutoReplyInput(
    val messageText: String,
    val senderName: String = "",
    val isFromOwner: Boolean = false
)
