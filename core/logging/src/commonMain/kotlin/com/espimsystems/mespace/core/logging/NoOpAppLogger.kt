package com.espimsystems.mespace.core.logging

object NoOpAppLogger : AppLogger {

    override fun verbose(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) = Unit

    override fun debug(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) = Unit

    override fun info(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) = Unit

    override fun warn(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) = Unit

    override fun error(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) = Unit
}