package com.espimsystems.mespace.core.logging

object AppLoggerFactory {

    fun create(
        minimumLevel: AppLogLevel = AppLogLevel.DEBUG,
        enabled: Boolean = true,
    ): AppLogger {
        return if (enabled) {
            KermitAppLogger(minimumLevel = minimumLevel)
        } else {
            NoOpAppLogger
        }
    }
}