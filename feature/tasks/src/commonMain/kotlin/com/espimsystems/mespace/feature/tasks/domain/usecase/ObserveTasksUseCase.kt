package com.espimsystems.mespace.feature.tasks.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(
    private val repository: TasksRepository,
) {

    operator fun invoke(spaceId: String): Flow<AppResult<List<Task>>> {
        require(spaceId.isNotBlank()) {
            AppResult.Failure(
                AppError.Validation(
                    field = FIELD_USER_ID,
                    message = "spaceId cannot be blank.",
                )
            )
        }

        return repository.observeTasks(spaceId)
    }

    private companion object {
        const val FIELD_USER_ID = "userId"
    }
}