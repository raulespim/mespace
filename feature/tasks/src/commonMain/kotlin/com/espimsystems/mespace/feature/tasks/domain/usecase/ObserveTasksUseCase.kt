package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveTasksUseCase(
    private val repository: TasksRepository,
) {

    operator fun invoke(spaceId: String): Flow<AppResult<List<Task>>> {
        if (spaceId.isBlank()) {
            return flowOf(
                AppResult.Failure(
                    AppError.Validation(
                        field = FIELD_SPACE_ID,
                        message = "Space id cannot be empty.",
                    ),
                ),
            )
        }

        return repository.observeTasks(spaceId)
    }

    private companion object {
        const val FIELD_SPACE_ID = "spaceId"
    }
}
