package com.espimsystems.mespace.core.common.time

interface ClockProvider {

    fun nowEpochMillis(): Long
}

object SystemClockProvider : ClockProvider {

    override fun nowEpochMillis(): Long {
        return currentTimeMillis()
    }
}

expect fun currentTimeMillis(): Long