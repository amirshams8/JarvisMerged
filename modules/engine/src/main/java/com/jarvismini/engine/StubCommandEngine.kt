package com.jarvismini.engine

object StubCommandEngine : CommandEngine {

    override fun execute(input: String): EngineResult {
        return EngineResult.Unhandled
    }
}
