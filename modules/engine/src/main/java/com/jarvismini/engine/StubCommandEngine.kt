package com.jarvismini.engine

object StubCommandEngine : CommandEngine {

    override fun execute(input: String): Boolean {
        return false
    }
}
