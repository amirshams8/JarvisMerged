package com.jarvismini

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var simulateButton: Button
    private lateinit var modelSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple UI setup
        statusText = TextView(this).apply {
            textSize = 16f
            text = "Jarvis status will appear here"
            setPadding(24, 24, 24, 24)
        }

        simulateButton = Button(this).apply {
            text = "Simulate Incoming Message"
            setOnClickListener { simulateIncomingMessage() }
        }

        // Spinner setup
        modelSpinner = Spinner(this)
        val models = listOf("Model A", "Model B", "Model C") // replace with your models
        modelSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, models)

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedModel = models[position]
                statusText.text = "Selected model: $selectedModel"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // optional
            }
        }

        // Linear layout to hold text, spinner, and button
        val layout = androidx.appcompat.widget.LinearLayoutCompat(this).apply {
            orientation = androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
            addView(statusText)
            addView(modelSpinner)
            addView(simulateButton)
        }

        setContentView(layout)
    }

    private fun simulateIncomingMessage() {
        val testMessage = "Hello Jarvis! Are you awake?"

        val input = AutoReplyInput(
            messageText = testMessage,
            isFromOwner = false
        )

        val decision = AutoReplyOrchestrator.handle(input)

        // Display the result
        statusText.text = when (decision) {
            is ReplyDecision.AutoReply -> "AutoReply: ${decision.message}"
            ReplyDecision.NoReply -> "No reply decision made"
        }
    }
}
