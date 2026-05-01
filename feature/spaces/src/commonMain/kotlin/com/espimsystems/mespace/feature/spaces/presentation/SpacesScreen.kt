package com.espimsystems.mespace.feature.spaces.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espimsystems.mespace.core.designsystem.component.MeSpaceButton
import com.espimsystems.mespace.core.designsystem.component.MeSpaceEmptyState
import com.espimsystems.mespace.core.designsystem.component.MeSpaceScaffold
import com.espimsystems.mespace.core.designsystem.component.MeSpaceSurface
import com.espimsystems.mespace.core.designsystem.component.MeSpaceTextButton
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme

@Composable
fun SpacesScreen(
    state: SpacesUiState,
    onIntent: (SpacesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MeSpaceScaffold { contentPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MeSpaceTheme.spacing.extraLarge),
            ) {
                Text(
                    text = "Espaços",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Organize tarefas com quem divide a rotina com você.",
                    modifier = Modifier.padding(top = MeSpaceTheme.spacing.small),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(MeSpaceTheme.spacing.extraLarge))

                when {
                    state.isLoading -> {
                        LoadingContent(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    state.spaces.isEmpty() -> {
                        EmptySpacesContent(
                            onCreateSpaceClick = {
                                onIntent(SpacesIntent.CreateSpaceClicked)
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> {
                        SpacesListContent(
                            spaces = state.spaces,
                            onSpaceClick = { space ->
                                onIntent(
                                    SpacesIntent.SpaceClicked(
                                        spaceId = space.id,
                                        spaceName = space.name,
                                    ),
                                )
                            },
                            onCreateSpaceClick = {
                                onIntent(SpacesIntent.CreateSpaceClicked)
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (state.isCreateSpaceDialogVisible) {
                CreateSpaceDialog(
                    spaceName = state.createSpaceName,
                    isCreating = state.isCreatingSpace,
                    canCreate = state.canCreateSpace,
                    errorMessage = state.errorMessage,
                    onNameChanged = { name ->
                        onIntent(SpacesIntent.CreateSpaceNameChanged(name))
                    },
                    onConfirmClick = {
                        onIntent(SpacesIntent.CreateSpaceConfirmed)
                    },
                    onDismissClick = {
                        onIntent(SpacesIntent.CreateSpaceDialogDismissed)
                    },
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptySpacesContent(
    onCreateSpaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        MeSpaceEmptyState(
            title = "Nenhum espaço ainda",
            description = "Crie um espaço para organizar tarefas com sua casa, família ou casal.",
            actionText = "Criar espaço",
            onActionClick = onCreateSpaceClick,
        )
    }
}

@Composable
private fun SpacesListContent(
    spaces: List<SpaceListItemUiModel>,
    onSpaceClick: (SpaceListItemUiModel) -> Unit,
    onCreateSpaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.medium),
    ) {
        items(
            items = spaces,
            key = { space -> space.id },
        ) { space ->
            SpaceListItem(
                space = space,
                onClick = {
                    onSpaceClick(space)
                },
            )
        }

        item {
            MeSpaceButton(
                text = "Criar novo espaço",
                onClick = onCreateSpaceClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MeSpaceTheme.spacing.medium),
            )
        }
    }
}

@Composable
private fun SpaceListItem(
    space: SpaceListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MeSpaceSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column {
            Text(
                text = space.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Toque para ver as tarefas deste espaço",
                modifier = Modifier.padding(top = MeSpaceTheme.spacing.extraSmall),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CreateSpaceDialog(
    spaceName: String,
    isCreating: Boolean,
    canCreate: Boolean,
    errorMessage: String?,
    onNameChanged: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = {
            Text(text = "Criar espaço")
        },
        text = {
            Column {
                Text(
                    text = "Dê um nome para o espaço compartilhado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = spaceName,
                    onValueChange = onNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MeSpaceTheme.spacing.large),
                    enabled = !isCreating,
                    singleLine = true,
                    label = {
                        Text(text = "Nome do espaço")
                    },
                    placeholder = {
                        Text(text = "Ex: Minha casa")
                    },
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(top = MeSpaceTheme.spacing.medium),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            MeSpaceButton(
                text = if (isCreating) "Criando..." else "Criar",
                onClick = onConfirmClick,
                enabled = canCreate,
            )
        },
        dismissButton = {
            MeSpaceTextButton(
                text = "Cancelar",
                onClick = onDismissClick,
                enabled = !isCreating,
            )
        },
    )
}