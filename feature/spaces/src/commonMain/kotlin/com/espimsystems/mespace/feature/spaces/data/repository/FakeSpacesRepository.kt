package com.espimsystems.mespace.feature.spaces.data.repository

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.spaces.domain.model.Space
import com.espimsystems.mespace.feature.spaces.domain.model.SpaceMember
import com.espimsystems.mespace.feature.spaces.domain.repository.SpacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.collections.orEmpty
import kotlin.collections.plus

class FakeSpacesRepository : SpacesRepository {

    private val spaces = MutableStateFlow<List<Space>>(emptyList())

    private val membersBySpaceId = MutableStateFlow<Map<String, List<SpaceMember>>>(emptyMap())

    override fun observeSpacesForUser(
        userId: String,
    ): Flow<AppResult<List<Space>>> {
        return combine(
            spaces,
            membersBySpaceId,
        ) { currentSpaces, currentMembersBySpaceId ->
            val userSpaces = currentSpaces
                .filter { space ->
                    val members = currentMembersBySpaceId[space.id].orEmpty()

                    !space.isArchived &&
                            members.any { member ->
                                member.userId == userId && member.isActive
                            }
                }
                .sortedByDescending { space ->
                    space.updatedAtEpochMillis
                }

            AppResult.Success(userSpaces)
        }
    }

    override fun observeMembers(
        spaceId: String,
    ): Flow<AppResult<List<SpaceMember>>> {
        return membersBySpaceId.map { currentMembersBySpaceId ->
            AppResult.Success(
                currentMembersBySpaceId[spaceId].orEmpty(),
            )
        }
    }

    override suspend fun getSpaceById(
        spaceId: String,
    ): AppResult<Space> {
        val space = spaces.value.firstOrNull { currentSpace ->
            currentSpace.id == spaceId && !currentSpace.isArchived
        }

        return if (space != null) {
            AppResult.Success(space)
        } else {
            AppResult.Failure(
                AppError.NotFound(
                    message = "Space not found.",
                ),
            )
        }
    }

    override suspend fun createSpace(
        space: Space,
        ownerMember: SpaceMember,
    ): AppResult<Space> {
        val alreadyExists = spaces.value.any { currentSpace ->
            currentSpace.id == space.id
        }

        if (alreadyExists) {
            return AppResult.Failure(
                AppError.Validation(
                    field = "id",
                    message = "Space already exists.",
                ),
            )
        }

        spaces.update { currentSpaces ->
            currentSpaces + space
        }

        membersBySpaceId.update { currentMembersBySpaceId ->
            val currentMembers = currentMembersBySpaceId[space.id].orEmpty()

            currentMembersBySpaceId + (
                    space.id to (currentMembers + ownerMember)
                    )
        }

        return AppResult.Success(space)
    }
}