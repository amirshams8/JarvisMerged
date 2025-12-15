package com.jarvismini.engine

object EngineProvider {

    val engine: LLMEngine by lazy {
        StubLLMEngine
    }
}
