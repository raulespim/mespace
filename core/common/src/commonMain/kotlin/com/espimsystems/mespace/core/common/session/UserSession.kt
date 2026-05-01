package com.espimsystems.mespace.core.common.session

data class UserSession(
    val userId: String,
    val displayName: String,
    val email: String?,
)