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
                    field = FIELD_SPACE_ID,
                    message = "Space id cannot be empty.",
                ),
            )
        }

        if (taskId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_TASK_ID,
                    message = "Task id cannot be empty.",
                ),
            )
        }

        return repository.deleteTask(
            spaceId = spaceId,
            taskId = taskId,
        )
    }

    private companion object {

        const val FIELD_SPACE_ID = "spaceId"
        const val FIELD_TASK_ID = "taskId"
    }
}
