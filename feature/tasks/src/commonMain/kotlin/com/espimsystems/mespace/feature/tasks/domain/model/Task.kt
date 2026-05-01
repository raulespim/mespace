package com.espimsystems.mespace.feature.tasks.domain.model

data class Task(
    val id: String,
    val spaceId: String,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedToUserId: String?,
    val createdByUserId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val completedAtMillis: Long?,
)