package com.espimsystems.mespace.core.logging

import co.touchlab.kermit.Logger

class KermitAppLogger(
    private val minimumLevel: AppLogLevel = AppLogLevel.DEBUG,
) : AppLogger {

    override fun verbose(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) {
        log(
            level = AppLogLevel.VERBOSE,
            tag = tag,
            throwable = throwable,
            message = message,
        )
    }

    override fun debug(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) {
        log(
            level = AppLogLevel.DEBUG,
            tag = tag,
            throwable = throwable,
            message = message,
        )
    }

    override fun info(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) {
        log(
            level = AppLogLevel.INFO,
            tag = tag,
            throwable = throwable,
            message = message,
        )
    }

    override fun warn(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) {
        log(
            level = AppLogLevel.WARN,
            tag = tag,
            throwable = throwable,
            message = message,
        )
    }

    override fun error(
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) {
        log(
            level = AppLogLevel.ERROR,
            tag = tag,
            throwable = throwable,
            message = message,
        )
    }

    private fun log(
        level: AppLogLevel,
        tag: AppLogTag,
        throwable: Throwable?,
        message: () -> String,
    ) {
        if (!minimumLevel.allows(level)) return

        when (level) {
            AppLogLevel.VERBOSE -> {
                Logger.v(
                    throwable = throwable,
                    tag = tag.value,
                ) {
                    message()
                }
            }

            AppLogLevel.DEBUG -> {
                Logger.d(
                    throwable = throwable,
                    tag = tag.value,
                ) {
                    message()
                }
            }

            AppLogLevel.INFO -> {
                Logger.i(
                    throwable = throwable,
                    tag = tag.value,
                ) {
                    message()
                }
            }

            AppLogLevel.WARN -> {
                Logger.w(
                    throwable = throwable,
                    tag = tag.value,
                ) {
                    message()
                }
            }

            AppLogLevel.ERROR -> {
                Logger.e(
                    throwable = throwable,
                    tag = tag.value,
                ) {
                    message()
                }
            }

            AppLogLevel.NONE -> Unit
        }
    }
}