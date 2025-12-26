package com.jarvismini

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jarvismini.automation.decision.ReplyDecision
import com.jarvismini.automation.input.AutoReplyInput
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var simulateButton: Button
    private lateinit var modeSpinner: Spinner

    companion object {
        private const val PERMISSION_REQ_CODE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()

        // Status text
        statusText = TextView(this).apply {
            textSize = 16f
            text = "Current mode: ${JarvisState.currentMode}"
            setPadding(24, 24, 24, 24)
        }

        // Spinner
        modeSpinner = Spinner(this)
        val modes = JarvisMode.values().map { it.name }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            modes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSpinner.adapter = adapter

        modeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedMode = JarvisMode.valueOf(modes[position])
                    JarvisState.currentMode = selectedMode
                    statusText.text = "Current mode: $selectedMode"
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // Button
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

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()

        // Android 13+ notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions += android.Manifest.permission.POST_NOTIFICATIONS
            }
        }

        // SMS + Contacts (Phase 1)
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions += android.Manifest.permission.RECEIVE_SMS
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions += android.Manifest.permission.SEND_SMS
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions += android.Manifest.permission.READ_CONTACTS
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQ_CODE
            )
        }
    }

    private fun simulateIncomingMessage() {
        val input = AutoReplyInput(
            messageText = "Hello Jarvis! Are you there?",
            isFromOwner = false
        )

        val decision = AutoReplyOrchestrator.handle(input)

        statusText.text = when (decision) {
            is ReplyDecision.AutoReply ->
                "AutoReply: ${decision.message}"
            ReplyDecision.NoReply ->
                "No reply decision made"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQ_CODE) {
            Toast.makeText(
                this,
                "Phase-1 permissions processed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
