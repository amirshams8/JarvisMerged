package com.jarvismini.automation

import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AutoReplyOrchestratorTest {

    @Before
    fun setup() {
        JarvisState.currentMode = JarvisMode.NORMAL
    }

    @After
    fun tearDown() {
        JarvisState.currentMode = JarvisMode.NORMAL
    }

    private fun baseContext(
        isOwner: Boolean = false,
        isGroup: Boolean = false,
        message: String = "Hello"
    ): AutoReplyContext {
        return AutoReplyContext(
            senderId = "123",
            senderName = "John",
            isOwner = isOwner,
            isGroupChat = isGroup,
            messageText = message,
            timestamp = System.currentTimeMillis()
        )
    }

    @Test
    fun `does not reply to owner`() {
        val reply = AutoReplyOrchestrator.handleIncomingMessage(
            baseContext(isOwner = true)
        )
        assertNull(reply)
    }

    @Test
    fun `does not reply in group chats`() {
        val reply = AutoReplyOrchestrator.handleIncomingMessage(
            baseContext(isGroup = true)
        )
        assertNull(reply)
    }

    @Test
    fun `ignores low signal messages`() {
        val reply = AutoReplyOrchestrator.handleIncomingMessage(
            baseContext(message = "ok")
        )
        assertNull(reply)
    }

    @Test
    fun `replies in normal mode for valid message`() {
        val reply = AutoReplyOrchestrator.handleIncomingMessage(
            baseContext(message = "Can you call me?")
        )
        assertNotNull(reply)
        assertTrue(reply!!.isNotBlank())
    }

    @Test
    fun `does not reply in sleep mode`() {
        JarvisState.currentMode = JarvisMode.SLEEP

        val reply = AutoReplyOrchestrator.handleIncomingMessage(
            baseContext()
        )
        assertNull(reply)
    }

    @Test
    fun `does not reply in focus mode`() {
        JarvisState.currentMode = JarvisMode.FOCUS

        val reply = AutoReplyOrchestrator.handleIncomingMessage(
            baseContext()
        )
        assertNull(reply)
    }
}
