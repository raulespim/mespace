package com.espimsystems.mespace.feature.spaces.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.spaces.domain.model.Space
import com.espimsystems.mespace.feature.spaces.domain.repository.SpacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveSpacesUseCase(
    private val spacesRepository: SpacesRepository,
) {

    operator fun invoke(
        userId: String,
    ): Flow<AppResult<List<Space>>> {
        if (userId.isBlank()) {
            return flowOf(
                AppResult.Failure(
                    AppError.Validation(
                        field = FIELD_USER_ID,
                        message = "User id cannot be empty.",
                    ),
                ),
            )
        }

        return spacesRepository.observeSpacesForUser(userId)
    }

    private companion object {

        const val FIELD_USER_ID = "userId"
    }
}