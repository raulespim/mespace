package com.espimsystems.mespace.feature.spaces.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.spaces.domain.model.SpaceMember
import com.espimsystems.mespace.feature.spaces.domain.repository.SpacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveSpaceMembersUseCase(
    private val spacesRepository: SpacesRepository,
) {

    operator fun invoke(
        spaceId: String,
    ): Flow<AppResult<List<SpaceMember>>> {
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

        return spacesRepository.observeMembers(spaceId)
    }

    private companion object {

        const val FIELD_SPACE_ID = "spaceId"
    }
}