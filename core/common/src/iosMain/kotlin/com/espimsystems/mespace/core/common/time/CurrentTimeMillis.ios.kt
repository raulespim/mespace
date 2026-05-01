package com.espimsystems.mespace.core.common.time

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1_000).toLong()
}