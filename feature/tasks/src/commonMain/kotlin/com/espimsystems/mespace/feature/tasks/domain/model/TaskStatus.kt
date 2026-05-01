package com.espimsystems.mespace.feature.tasks.domain.model

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    DONE;

    fun next(): TaskStatus {
        return when (this) {
            PENDING -> IN_PROGRESS
            IN_PROGRESS -> DONE
            DONE -> PENDING
        }
    }
}