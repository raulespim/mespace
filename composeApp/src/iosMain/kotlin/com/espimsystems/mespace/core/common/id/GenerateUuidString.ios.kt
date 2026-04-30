package com.espimsystems.mespace.core.common.id

import platform.Foundation.NSUUID

actual fun generateUuidString(): String {
    return NSUUID().UUIDString.lowercase()
}