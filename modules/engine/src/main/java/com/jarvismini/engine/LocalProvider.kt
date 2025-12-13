package com.jarvismini.engine

class LocalProvider : LLMProvider {
    override suspend fun generateReply(prompt: String): String {
        return "Local reply: $prompt"
    }
}
