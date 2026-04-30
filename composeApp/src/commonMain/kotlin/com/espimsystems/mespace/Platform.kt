package com.espimsystems.mespace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform