package com.jarvismini.automation.orchestrator

import com.jarvismini.automation.decision.AutoReplyDecision
import com.jarvismini.automation.decision.NoReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState

/**
 * Orchestrator for auto-replies.
 * Decides whether Jarvis should reply, notify, or ignore.
 */
object AutoReplyOrchestrator {

    /**
     * Processes an incoming message and returns a decision.
     */
    fun handle(input: AutoReplyInput): Any { // Using Any for build-safety; later can use sealed class
        // Mode check
        return when (JarvisState.currentMode) {
            JarvisMode.SLEEP, JarvisMode.FOCUS -> NoReplyDecision
            JarvisMode.DRIVING -> AutoReplyDecision(
                message = "I'm driving. I'll get back to you shortly."
            )
            JarvisMode.WORK -> AutoReplyDecision(
                message = "I'm working now. I will respond later."
            )
            JarvisMode.NORMAL -> NoReplyDecision
        }
    }

    /**
     * Optional init for future context wiring.
     */
    fun init() {
        // Placeholder for any future startup setup
    }
}
