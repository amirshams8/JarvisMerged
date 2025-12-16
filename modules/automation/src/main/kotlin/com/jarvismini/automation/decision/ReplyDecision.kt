package com.jarvismini.automation.decision

sealed class ReplyDecision

object NoReplyDecision : ReplyDecision()

data class AutoReplyDecision(
    val message: String,
    val reason: String? = null
) : ReplyDecision()
