package com.espimsystems.mespace.core.common.id

interface IdGenerator {

    fun generateId(): String
}

object UuidGenerator : IdGenerator {

    override fun generateId(): String {
        return generateUuidString()
    }
}

expect fun generateUuidString(): String