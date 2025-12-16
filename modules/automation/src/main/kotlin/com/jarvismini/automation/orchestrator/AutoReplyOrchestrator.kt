package com.jarvismini.automation.orchestrator

import com.jarvismini.automation.decision.*
import com.jarvismini.automation.input.AutoReplyInput

object AutoReplyOrchestrator {

    fun process(input: AutoReplyInput): ReplyDecision {
        return NoReplyDecision
    }

    fun sendResponse(message: String) {
        // stub
    }
}
