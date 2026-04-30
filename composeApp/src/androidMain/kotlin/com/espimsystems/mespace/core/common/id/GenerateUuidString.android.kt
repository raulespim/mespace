package com.espimsystems.mespace.core.common.id

import java.util.UUID

actual fun generateUuidString(): String {
    return UUID.randomUUID().toString()
}