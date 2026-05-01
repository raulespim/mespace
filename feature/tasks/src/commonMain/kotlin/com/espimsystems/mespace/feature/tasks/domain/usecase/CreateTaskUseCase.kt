package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository

class CreateTaskUseCase(
    private val repository: TasksRepository,
) {

    suspend operator fun invoke(input: CreateTaskInput): AppResult<Task> {
        val title = input.title.trim()
        val description = input.description
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (input.spaceId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "spaceId cannot be blank.",
                    cause = IllegalArgumentException()
                )
            )
        }

        if (title.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "Task title cannot be blank.",
                    cause = IllegalArgumentException()
                ),
            )
        }

        if (input.createdByUserId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "createdByUserId cannot be blank.",
                    cause = IllegalArgumentException()
                ),
            )
        }

        val task = Task(
            id = input.id,
            spaceId = input.spaceId,
            title = title,
            description = description,
            status = TaskStatus.PENDING,
            priority = input.priority,
            assignedToUserId = input.assignedToUserId,
            createdByUserId = input.createdByUserId,
            createdAtMillis = input.createdAtMillis,
            updatedAtMillis = input.createdAtMillis,
            completedAtMillis = null,
        )

        return repository.createTask(task)
    }
}

data class CreateTaskInput(
    val id: String,
    val spaceId: String,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val assignedToUserId: String?,
    val createdByUserId: String,
    val createdAtMillis: Long,
)