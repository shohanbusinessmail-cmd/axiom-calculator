package com.axiom.calculator.core.util

/**
 * Injectable clock abstraction (SECTION 19). Production code uses
 * [SystemAxClock]; tests inject a fixed/fake clock so retention/expiration
 * logic is deterministic and never depends on the real wall-clock time.
 */
interface AxClock {
    fun nowEpochMillis(): Long
}

object SystemAxClock : AxClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}

class FixedAxClock(private var epochMillis: Long) : AxClock {
    override fun nowEpochMillis(): Long = epochMillis
    fun advanceBy(deltaMillis: Long) { epochMillis += deltaMillis }
    fun set(epochMillis: Long) { this.epochMillis = epochMillis }
}
