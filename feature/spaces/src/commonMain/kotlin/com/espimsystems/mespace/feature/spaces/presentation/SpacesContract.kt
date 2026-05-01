package com.espimsystems.mespace.feature.spaces.presentation

import com.espimsystems.mespace.core.common.mvi.UiEffect
import com.espimsystems.mespace.core.common.mvi.UiIntent
import com.espimsystems.mespace.core.common.mvi.UiState

data class SpacesUiState(
    val isLoading: Boolean = true,
    val spaces: List<SpaceListItemUiModel> = emptyList(),
    val isCreateSpaceDialogVisible: Boolean = false,
    val createSpaceName: String = "",
    val isCreatingSpace: Boolean = false,
    val errorMessage: String? = null,
) : UiState {

    val canCreateSpace: Boolean
        get() = createSpaceName.isNotBlank() && !isCreatingSpace
}

data class SpaceListItemUiModel(
    val id: String,
    val name: String,
)

sealed interface SpacesIntent : UiIntent {

    data object CreateSpaceClicked : SpacesIntent

    data object CreateSpaceDialogDismissed : SpacesIntent

    data class CreateSpaceNameChanged(
        val name: String,
    ) : SpacesIntent

    data object CreateSpaceConfirmed : SpacesIntent

    data class SpaceClicked(
        val spaceId: String,
        val spaceName: String,
    ) : SpacesIntent

    data object ErrorMessageShown : SpacesIntent
}

sealed interface SpacesEffect : UiEffect {

    data class NavigateToTasks(
        val spaceId: String,
        val spaceName: String,
    ) : SpacesEffect
}