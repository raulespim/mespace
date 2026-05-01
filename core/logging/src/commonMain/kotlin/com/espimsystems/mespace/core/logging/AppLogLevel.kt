package com.espimsystems.mespace.core.logging

enum class AppLogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE;

    internal fun allows(level: AppLogLevel): Boolean {
        if (this == NONE) return false
        return level.ordinal >= this.ordinal
    }
}