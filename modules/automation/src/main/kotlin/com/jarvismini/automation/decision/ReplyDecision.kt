package com.jarvismini.automation.decision

/**
 * Final decision returned by automation layer.
 */
sealed class ReplyDecision {

    /**
     * Jarvis should auto-reply with this message.
     */
    data class AutoReply(
        val message: String
    ) : ReplyDecision()

    /**
     * Jarvis should not reply.
     */
    object NoReply : ReplyDecision()
}
