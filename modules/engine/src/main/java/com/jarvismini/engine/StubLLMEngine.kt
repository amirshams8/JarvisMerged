package com.jarvismini.engine

import android.content.Context
import com.jarvismini.core.JarvisState

object StubLLMEngine : LLMEngine {

    override fun init(context: Context) {
        // no-op
    }

    override fun generateReply(input: AutoReplyInput): String {
        return ReplyToneResolver.resolve(
            reason = input.reason,
            mode = JarvisState.currentMode
        )
    }
}
