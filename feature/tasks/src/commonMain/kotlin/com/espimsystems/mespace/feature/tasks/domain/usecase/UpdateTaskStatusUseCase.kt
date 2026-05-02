package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.time.ClockProvider
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository

class UpdateTaskStatusUseCase(
    private val repository: TasksRepository,
    private val clockProvider: ClockProvider,
) {

    suspend operator fun invoke(input: UpdateTaskStatusInput): AppResult<Task> {
        if (input.spaceId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_SPACE_ID,
                    message = "Space id cannot be empty.",
                ),
            )
        }

        if (input.taskId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_TASK_ID,
                    message = "Task id cannot be empty.",
                ),
            )
        }

        return repository.updateTaskStatus(
            spaceId = input.spaceId,
            taskId = input.taskId,
            status = input.status,
            updatedAtMillis = clockProvider.nowEpochMillis(),
        )
    }

    private companion object {

        const val FIELD_SPACE_ID = "spaceId"
        const val FIELD_TASK_ID = "taskId"
    }
}

data class UpdateTaskStatusInput(
    val spaceId: String,
    val taskId: String,
    val status: TaskStatus,
)
