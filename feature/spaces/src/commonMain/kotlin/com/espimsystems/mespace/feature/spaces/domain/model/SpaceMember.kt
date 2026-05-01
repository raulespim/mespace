package com.espimsystems.mespace.feature.spaces.domain.model

data class SpaceMember(
    val id: String,
    val spaceId: String,
    val userId: String,
    val displayName: String,
    val email: String?,
    val role: SpaceRole,
    val joinedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isActive: Boolean = true,
)