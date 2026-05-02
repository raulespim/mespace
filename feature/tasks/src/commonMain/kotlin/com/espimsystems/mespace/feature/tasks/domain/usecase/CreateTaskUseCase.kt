package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.id.IdGenerator
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.time.ClockProvider
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository

class CreateTaskUseCase(
    private val repository: TasksRepository,
    private val idGenerator: IdGenerator,
    private val clockProvider: ClockProvider,
) {

    suspend operator fun invoke(input: CreateTaskInput): AppResult<Task> {
        val title = input.title.normalizeTitle()
        val description = input.description
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (input.spaceId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_SPACE_ID,
                    message = "Space id cannot be empty.",
                ),
            )
        }

        if (title.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_TITLE,
                    message = "Task title cannot be empty.",
                ),
            )
        }

        if (title.length > TASK_TITLE_MAX_LENGTH) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_TITLE,
                    message = "Task title cannot be longer than $TASK_TITLE_MAX_LENGTH characters.",
                ),
            )
        }

        if (description != null && description.length > TASK_DESCRIPTION_MAX_LENGTH) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_DESCRIPTION,
                    message = "Task description cannot be longer than $TASK_DESCRIPTION_MAX_LENGTH characters.",
                ),
            )
        }

        if (input.createdByUserId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_CREATED_BY_USER_ID,
                    message = "Creator user id cannot be empty.",
                ),
            )
        }

        val now = clockProvider.nowEpochMillis()

        val task = Task(
            id = idGenerator.generateId(),
            spaceId = input.spaceId,
            title = title,
            description = description,
            status = TaskStatus.PENDING,
            priority = input.priority,
            assignedToUserId = input.assignedToUserId,
            createdByUserId = input.createdByUserId,
            createdAtMillis = now,
            updatedAtMillis = now,
            completedAtMillis = null,
        )

        return repository.createTask(task)
    }

    private fun String.normalizeTitle(): String {
        return trim()
            .replace(Regex("\\s+"), " ")
    }

    private companion object {

        const val FIELD_SPACE_ID = "spaceId"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CREATED_BY_USER_ID = "createdByUserId"

        const val TASK_TITLE_MAX_LENGTH = 80
        const val TASK_DESCRIPTION_MAX_LENGTH = 280
    }
}

data class CreateTaskInput(
    val spaceId: String,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val assignedToUserId: String?,
    val createdByUserId: String,
)
