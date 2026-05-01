package com.espimsystems.mespace.core.logging

interface AppLogger {

    fun verbose(
        tag: AppLogTag,
        throwable: Throwable? = null,
        message: () -> String,
    )

    fun debug(
        tag: AppLogTag,
        throwable: Throwable? = null,
        message: () -> String,
    )

    fun info(
        tag: AppLogTag,
        throwable: Throwable? = null,
        message: () -> String,
    )

    fun warn(
        tag: AppLogTag,
        throwable: Throwable? = null,
        message: () -> String,
    )

    fun error(
        tag: AppLogTag,
        throwable: Throwable? = null,
        message: () -> String,
    )
}