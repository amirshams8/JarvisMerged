package com.jarvismini

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var simulateButton: Button
    private lateinit var modeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Status TextView
        statusText = TextView(this).apply {
            textSize = 16f
            text = "Jarvis status will appear here"
            setPadding(24, 24, 24, 24)
        }

        // Spinner to switch modes
        modeSpinner = Spinner(this)
        val modes = JarvisMode.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSpinner.adapter = adapter
        modeSpinner.setSelection(JarvisState.currentMode.ordinal)
        modeSpinner.setOnItemSelectedListener { _, _, position, _ ->
            JarvisState.currentMode = JarvisMode.values()[position]
            statusText.text = "Current Mode: ${JarvisState.currentMode}"
        }

        // Simulate Button
        simulateButton = Button(this).apply {
            text = "Simulate Incoming Message"
            setOnClickListener { simulateIncomingMessage() }
        }

        // Layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(statusText)
            addView(modeSpinner)
            addView(simulateButton)
        }

        setContentView(layout)
    }

    private fun simulateIncomingMessage() {
        val testMessage = "Hello Jarvis! Are you awake?"
        val input = AutoReplyInput(messageText = testMessage, isFromOwner = false)
        val decision = AutoReplyOrchestrator.handle(input)

        statusText.text = when (decision) {
            is ReplyDecision.AutoReply -> "AutoReply: ${decision.message}"
            ReplyDecision.NoReply -> "No reply decision made"
        }
    }
}
