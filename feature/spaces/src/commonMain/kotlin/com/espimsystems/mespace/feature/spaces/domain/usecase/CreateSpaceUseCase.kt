package com.espimsystems.mespace.feature.spaces.domain.usecase

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.id.IdGenerator
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.time.ClockProvider
import com.espimsystems.mespace.feature.spaces.domain.model.Space
import com.espimsystems.mespace.feature.spaces.domain.model.SpaceMember
import com.espimsystems.mespace.feature.spaces.domain.model.SpaceRole
import com.espimsystems.mespace.feature.spaces.domain.repository.SpacesRepository

class CreateSpaceUseCase(
    private val spacesRepository: SpacesRepository,
    private val idGenerator: IdGenerator,
    private val clockProvider: ClockProvider,
) {

    suspend operator fun invoke(
        input: CreateSpaceInput,
    ): AppResult<Space> {
        val normalizedName = input.name.normalizeSpaceName()

        if (normalizedName.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_NAME,
                    message = "Space name cannot be empty.",
                ),
            )
        }

        if (normalizedName.length > SPACE_NAME_MAX_LENGTH) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_NAME,
                    message = "Space name cannot be longer than $SPACE_NAME_MAX_LENGTH characters.",
                ),
            )
        }

        if (input.ownerUserId.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_OWNER_USER_ID,
                    message = "Owner user id cannot be empty.",
                ),
            )
        }

        if (input.ownerDisplayName.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_OWNER_DISPLAY_NAME,
                    message = "Owner display name cannot be empty.",
                ),
            )
        }

        val now = clockProvider.nowEpochMillis()
        val spaceId = idGenerator.generateId() // TODO check later if need improve generateId

        val space = Space(
            id = spaceId,
            name = normalizedName,
            ownerUserId = input.ownerUserId,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )

        val ownerMember = SpaceMember(
            id = idGenerator.generateId(),
            spaceId = spaceId,
            userId = input.ownerUserId,
            displayName = input.ownerDisplayName.trim(),
            email = input.ownerEmail?.trim()?.takeIf { it.isNotBlank() },
            role = SpaceRole.OWNER,
            joinedAtEpochMillis = now,
            updatedAtEpochMillis = now,
            isActive = true,
        )

        return spacesRepository.createSpace(
            space = space,
            ownerMember = ownerMember,
        )
    }

    private fun String.normalizeSpaceName(): String {
        return trim()
            .replace(Regex("\\s+"), " ")
    }

    private companion object {

        const val FIELD_NAME = "name"
        const val FIELD_OWNER_USER_ID = "ownerUserId"
        const val FIELD_OWNER_DISPLAY_NAME = "ownerDisplayName"

        const val SPACE_NAME_MAX_LENGTH = 48
    }
}

data class CreateSpaceInput(
    val name: String,
    val ownerUserId: String,
    val ownerDisplayName: String,
    val ownerEmail: String? = null,
)