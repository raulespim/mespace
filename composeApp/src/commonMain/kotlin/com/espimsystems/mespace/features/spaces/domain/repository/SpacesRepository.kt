package com.espimsystems.mespace.features.spaces.domain.repository

import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.features.spaces.domain.model.Space
import com.espimsystems.mespace.features.spaces.domain.model.SpaceMember
import kotlinx.coroutines.flow.Flow

interface SpacesRepository {

    fun observeSpacesForUser(
        userId: String,
    ): Flow<AppResult<List<Space>>>

    fun observeMembers(
        spaceId: String,
    ): Flow<AppResult<List<SpaceMember>>>

    suspend fun getSpaceById(
        spaceId: String,
    ): AppResult<Space>

    suspend fun createSpace(
        space: Space,
        ownerMember: SpaceMember,
    ): AppResult<Space>
}