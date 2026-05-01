package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository

class UpdateTaskStatusUseCase(
    private val repository: TasksRepository,
) {

    suspend operator fun invoke(input: UpdateTaskStatusInput): AppResult<Task> {
        if (input.spaceId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "spaceId cannot be blank.",
                    cause = IllegalArgumentException()
                ),
            )
        }

        if (input.taskId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "taskId cannot be blank.",
                    cause = IllegalArgumentException()
                ),
            )
        }

        return repository.updateTaskStatus(
            spaceId = input.spaceId,
            taskId = input.taskId,
            status = input.status,
            updatedAtMillis = input.updatedAtMillis,
        )
    }
}

data class UpdateTaskStatusInput(
    val spaceId: String,
    val taskId: String,
    val status: TaskStatus,
    val updatedAtMillis: Long,
)