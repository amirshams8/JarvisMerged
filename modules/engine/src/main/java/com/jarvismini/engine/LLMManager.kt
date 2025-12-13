package com.jarvismini.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LLMManager(
    private val provider: LLMProvider
) {
    suspend fun ask(prompt: String): String =
        withContext(Dispatchers.IO) {
            provider.generateReply(prompt)
        }
}
