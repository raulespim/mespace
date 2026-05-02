package com.espimsystems.mespace.feature.spaces.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.database.MeSpaceDatabase
import com.espimsystems.mespace.feature.spaces.domain.model.Space
import com.espimsystems.mespace.feature.spaces.domain.model.SpaceMember
import com.espimsystems.mespace.feature.spaces.domain.model.SpaceRole
import com.espimsystems.mespace.feature.spaces.domain.repository.SpacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightSpacesRepository(
    private val database: MeSpaceDatabase,
    private val appDispatchers: AppDispatchers,
) : SpacesRepository {

    override fun observeSpacesForUser(
        userId: String,
    ): Flow<AppResult<List<Space>>> {
        return database.spacesQueries
            .selectSpacesForUser(userId, ::mapSpace)
            .asFlow()
            .mapToList(appDispatchers.io)
            .map<List<Space>, AppResult<List<Space>>> { spaces ->
                AppResult.Success(spaces)
            }
            .catch { throwable ->
                emit(storageFailure(throwable))
            }
    }

    override fun observeMembers(
        spaceId: String,
    ): Flow<AppResult<List<SpaceMember>>> {
        return database.spaceMembersQueries
            .selectMembersBySpaceId(spaceId, ::mapSpaceMember)
            .asFlow()
            .mapToList(appDispatchers.io)
            .map<List<SpaceMember>, AppResult<List<SpaceMember>>> { members ->
                AppResult.Success(members)
            }
            .catch { throwable ->
                emit(storageFailure(throwable))
            }
    }

    override suspend fun getSpaceById(
        spaceId: String,
    ): AppResult<Space> {
        return withContext(appDispatchers.io) {
            try {
                val space = database.spacesQueries
                    .selectSpaceById(spaceId, ::mapSpace)
                    .executeAsOneOrNull()

                if (space != null) {
                    AppResult.Success(space)
                } else {
                    AppResult.Failure(
                        AppError.NotFound(
                            message = "Space not found.",
                        ),
                    )
                }
            } catch (throwable: Throwable) {
                storageFailure(throwable)
            }
        }
    }

    override suspend fun createSpace(
        space: Space,
        ownerMember: SpaceMember,
    ): AppResult<Space> {
        return withContext(appDispatchers.io) {
            try {
                database.transactionWithResult {
                    val alreadyExists = database.spacesQueries
                        .selectSpaceByIdIncludingArchived(space.id)
                        .executeAsOneOrNull() != null

                    if (alreadyExists) {
                        AppResult.Failure(
                            AppError.Validation(
                                field = "id",
                                message = "Space already exists.",
                            ),
                        )
                    } else {
                        database.spacesQueries.insertSpace(
                            id = space.id,
                            name = space.name,
                            ownerUserId = space.ownerUserId,
                            createdAtEpochMillis = space.createdAtEpochMillis,
                            updatedAtEpochMillis = space.updatedAtEpochMillis,
                            archivedAtEpochMillis = space.archivedAtEpochMillis,
                        )
                        database.spaceMembersQueries.insertSpaceMember(
                            id = ownerMember.id,
                            spaceId = ownerMember.spaceId,
                            userId = ownerMember.userId,
                            displayName = ownerMember.displayName,
                            email = ownerMember.email,
                            role = ownerMember.role.name,
                            joinedAtEpochMillis = ownerMember.joinedAtEpochMillis,
                            updatedAtEpochMillis = ownerMember.updatedAtEpochMillis,
                            isActive = ownerMember.isActive.toLong(),
                        )

                        AppResult.Success(space)
                    }
                }
            } catch (throwable: Throwable) {
                storageFailure(throwable)
            }
        }
    }

    private fun mapSpace(
        id: String,
        name: String,
        ownerUserId: String,
        createdAtEpochMillis: Long,
        updatedAtEpochMillis: Long,
        archivedAtEpochMillis: Long?,
    ): Space {
        return Space(
            id = id,
            name = name,
            ownerUserId = ownerUserId,
            createdAtEpochMillis = createdAtEpochMillis,
            updatedAtEpochMillis = updatedAtEpochMillis,
            archivedAtEpochMillis = archivedAtEpochMillis,
        )
    }

    private fun mapSpaceMember(
        id: String,
        spaceId: String,
        userId: String,
        displayName: String,
        email: String?,
        role: String,
        joinedAtEpochMillis: Long,
        updatedAtEpochMillis: Long,
        isActive: Long,
    ): SpaceMember {
        return SpaceMember(
            id = id,
            spaceId = spaceId,
            userId = userId,
            displayName = displayName,
            email = email,
            role = SpaceRole.valueOf(role),
            joinedAtEpochMillis = joinedAtEpochMillis,
            updatedAtEpochMillis = updatedAtEpochMillis,
            isActive = isActive == ACTIVE_VALUE,
        )
    }

    private fun Boolean.toLong(): Long {
        return if (this) {
            ACTIVE_VALUE
        } else {
            INACTIVE_VALUE
        }
    }

    private fun storageFailure(throwable: Throwable): AppResult.Failure {
        return AppResult.Failure(
            AppError.Storage(
                message = STORAGE_ERROR_MESSAGE,
                cause = throwable,
            ),
        )
    }

    private companion object {

        const val ACTIVE_VALUE = 1L
        const val INACTIVE_VALUE = 0L
        const val STORAGE_ERROR_MESSAGE = "Unable to access local database."
    }
}
