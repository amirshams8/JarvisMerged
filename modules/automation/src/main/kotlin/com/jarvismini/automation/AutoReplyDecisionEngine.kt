package com.jarvismini.automation

import com.jarvismini.automation.decision.AutoReplyDecision
import com.jarvismini.automation.decision.NoReplyDecision
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.ModeGuard

object AutoReplyDecisionEngine {

    fun decide(input: AutoReplyInput): ReplyDecision {
        if (!ModeGuard.allowsReply()) {
            return NoReplyDecision
        }

        if (input.isFromOwner) {
            return NoReplyDecision
        }

        return AutoReplyDecision(
            message = "I’m busy right now. I’ll get back to you soon."
        )
    }
}
