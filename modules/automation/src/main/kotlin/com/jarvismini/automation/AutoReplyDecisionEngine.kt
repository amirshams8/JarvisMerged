package com.jarvismini.automation

import com.jarvismini.automation.decision.*
import com.jarvismini.automation.input.AutoReplyInput

object AutoReplyDecisionEngine {

    fun decide(input: AutoReplyInput): ReplyDecision {
        if (!ModeGuard.allowsReply()) {
            return NoReplyDecision
        }

        if (!input.isOwner) {
            return NoReplyDecision
        }

        return AutoReplyDecision(
            message = "I'll get back to you shortly.",
            reason = "Auto-reply enabled"
        )
    }
}
