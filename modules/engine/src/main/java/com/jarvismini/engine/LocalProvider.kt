package com.jarvismini.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalProvider : LLMProvider {

    override suspend fun generateReply(prompt: String): String =
        withContext(Dispatchers.Default) {
            // Simple local stub response
            "Local reply received: \"$prompt\""
        }
}
