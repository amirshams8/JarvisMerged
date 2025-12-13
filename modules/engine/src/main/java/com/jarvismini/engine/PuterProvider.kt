package com.jarvismini.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PuterProvider(
    private val apiKey: String? = null
) : LLMProvider {

    override suspend fun generateReply(prompt: String): String =
        withContext(Dispatchers.IO) {
            // Placeholder for future Puter API integration
            if (apiKey.isNullOrEmpty()) {
                "PuterProvider not configured"
            } else {
                "PuterProvider response for: \"$prompt\""
            }
        }
}
