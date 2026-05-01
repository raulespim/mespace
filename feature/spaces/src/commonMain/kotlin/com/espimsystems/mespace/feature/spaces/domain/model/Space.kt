package com.espimsystems.mespace.feature.spaces.domain.model

data class Space(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val archivedAtEpochMillis: Long? = null,
) {

    val isArchived: Boolean
        get() = archivedAtEpochMillis != null
}