package com.espimsystems.mespace.core.logging

import kotlin.jvm.JvmInline

@JvmInline
value class AppLogTag(val value: String) {
    init {
        require(value.isNotBlank()) {
            "AppLogTag value cannot be blank."
        }
    }
}