package com.espimsystems.mespace.features.spaces.presentation

import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.mvi.MviComponent
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.session.UserSession
import com.espimsystems.mespace.features.spaces.domain.model.Space
import com.espimsystems.mespace.features.spaces.domain.usecase.CreateSpaceInput
import com.espimsystems.mespace.features.spaces.domain.usecase.CreateSpaceUseCase
import com.espimsystems.mespace.features.spaces.domain.usecase.ObserveSpacesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest

class SpacesComponent(
    private val currentUser: UserSession,
    private val observeSpacesUseCase: ObserveSpacesUseCase,
    private val createSpaceUseCase: CreateSpaceUseCase,
    componentScope: CoroutineScope,
    dispatchers: AppDispatchers,
) : MviComponent<SpacesUiState, SpacesIntent, SpacesEffect>(
    initialState = SpacesUiState(),
    componentScope = componentScope,
    dispatchers = dispatchers,
) {

    init {
        observeSpaces()
    }

    override fun handleIntent(intent: SpacesIntent) {
        when (intent) {
            SpacesIntent.CreateSpaceClicked -> handleCreateSpaceClicked()
            SpacesIntent.CreateSpaceDialogDismissed -> handleCreateSpaceDialogDismissed()
            is SpacesIntent.CreateSpaceNameChanged -> handleCreateSpaceNameChanged(intent)
            SpacesIntent.CreateSpaceConfirmed -> handleCreateSpaceConfirmed()
            is SpacesIntent.SpaceClicked -> handleSpaceClicked(intent)
            SpacesIntent.ErrorMessageShown -> handleErrorMessageShown()
        }
    }

    private fun observeSpaces() {
        updateState {
            copy(isLoading = true)
        }

        launchSafely(
            dispatcher = dispatchers.main,
            onError = { throwable ->
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load spaces.",
                    )
                }
            },
        ) {
            observeSpacesUseCase(currentUser.userId).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        updateState {
                            copy(
                                isLoading = false,
                                spaces = result.data.toUiModels(),
                                errorMessage = null,
                            )
                        }
                    }

                    is AppResult.Failure -> {
                        updateState {
                            copy(
                                isLoading = false,
                                errorMessage = result.error.toUserMessage(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleCreateSpaceClicked() {
        updateState {
            copy(
                isCreateSpaceDialogVisible = true,
                createSpaceName = "",
                errorMessage = null,
            )
        }
    }

    private fun handleCreateSpaceDialogDismissed() {
        if (state.value.isCreatingSpace) return

        updateState {
            copy(
                isCreateSpaceDialogVisible = false,
                createSpaceName = "",
                errorMessage = null,
            )
        }
    }

    private fun handleCreateSpaceNameChanged(
        intent: SpacesIntent.CreateSpaceNameChanged,
    ) {
        updateState {
            copy(
                createSpaceName = intent.name,
                errorMessage = null,
            )
        }
    }

    private fun handleCreateSpaceConfirmed() {
        val currentState = state.value

        if (!currentState.canCreateSpace) return

        updateState {
            copy(
                isCreatingSpace = true,
                errorMessage = null,
            )
        }

        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                updateState {
                    copy(
                        isCreatingSpace = false,
                        errorMessage = throwable.message ?: "Unable to create space.",
                    )
                }
            },
        ) {
            val result = createSpaceUseCase(
                CreateSpaceInput(
                    name = currentState.createSpaceName,
                    ownerUserId = currentUser.userId,
                    ownerDisplayName = currentUser.displayName,
                    ownerEmail = currentUser.email,
                ),
            )

            when (result) {
                is AppResult.Success -> {
                    updateState {
                        copy(
                            isCreatingSpace = false,
                            isCreateSpaceDialogVisible = false,
                            createSpaceName = "",
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    updateState {
                        copy(
                            isCreatingSpace = false,
                            errorMessage = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun handleSpaceClicked(
        intent: SpacesIntent.SpaceClicked,
    ) {
        sendEffect(
            SpacesEffect.NavigateToTasks(
                spaceId = intent.spaceId,
                spaceName = intent.spaceName,
            ),
        )
    }

    private fun handleErrorMessageShown() {
        updateState {
            copy(errorMessage = null)
        }
    }

    private fun List<Space>.toUiModels(): List<SpaceListItemUiModel> {
        return map { space ->
            SpaceListItemUiModel(
                id = space.id,
                name = space.name,
            )
        }
    }

    private fun AppError.toUserMessage(): String {
        return when (this) {
            is AppError.Network -> "Check your internet connection and try again."
            is AppError.Unauthorized -> "You need to sign in again."
            is AppError.Forbidden -> "You do not have permission to do this."
            is AppError.NotFound -> "This space was not found."
            is AppError.Validation -> message ?: "Please check the information and try again."
            is AppError.Storage -> "Unable to save your data right now."
            is AppError.Unknown -> "Something went wrong. Please try again."
        }
    }
}