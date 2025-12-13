package com.jarvismini.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PuterProvider : LLMProvider {

    override suspend fun generateReply(prompt: String): String =
        withContext(Dispatchers.IO) {
            "PuterProvider response for: $prompt"
        }
}
