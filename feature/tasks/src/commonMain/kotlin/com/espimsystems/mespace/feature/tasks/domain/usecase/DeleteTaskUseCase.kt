package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository

class DeleteTaskUseCase(
    private val repository: TasksRepository,
) {

    suspend operator fun invoke(
        spaceId: String,
        taskId: String,
    ): AppResult<Unit> {
        if (spaceId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "spaceId cannot be blank.",
                    cause = IllegalArgumentException()
                ),
            )
        }

        if (taskId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "taskId cannot be blank.",
                    cause = IllegalArgumentException()
                ),
            )
        }

        return repository.deleteTask(
            spaceId = spaceId,
            taskId = taskId,
        )
    }
}