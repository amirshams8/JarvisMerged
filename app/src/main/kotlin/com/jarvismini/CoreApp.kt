package com.jarvismini

import android.app.Application
import com.jarvismini.automation.orchestrator.AutoReplyOrchestrator
import com.jarvismini.core.JarvisMode
import com.jarvismini.core.JarvisState

class CoreApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Explicit default mode
        JarvisState.currentMode = JarvisMode.NORMAL

        AutoReplyOrchestrator.init()

        println("CoreApp started with mode: ${JarvisState.currentMode}")
    }
}
