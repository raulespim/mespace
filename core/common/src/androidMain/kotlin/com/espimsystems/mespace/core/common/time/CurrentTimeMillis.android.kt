package com.espimsystems.mespace.core.common.time

actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}