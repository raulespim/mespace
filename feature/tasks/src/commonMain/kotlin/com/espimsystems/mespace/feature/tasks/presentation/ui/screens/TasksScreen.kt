package com.espimsystems.mespace.feature.tasks.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espimsystems.mespace.core.designsystem.component.MeSpaceEmptyState
import com.espimsystems.mespace.core.designsystem.component.MeSpaceScaffold
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme
import com.espimsystems.mespace.feature.tasks.presentation.TaskListItemUiModel
import com.espimsystems.mespace.feature.tasks.presentation.TasksIntent
import com.espimsystems.mespace.feature.tasks.presentation.TasksUiState
import com.espimsystems.mespace.feature.tasks.presentation.ui.components.CreateTaskDialog
import com.espimsystems.mespace.feature.tasks.presentation.ui.components.DeleteTaskConfirmationDialog
import com.espimsystems.mespace.feature.tasks.presentation.ui.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    state: TasksUiState,
    onIntent: (TasksIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.generalErrorMessage) {
        val message = state.generalErrorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onIntent(TasksIntent.GeneralErrorConsumed)
    }

    MeSpaceScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = state.spaceName)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onIntent(TasksIntent.BackClicked)
                        },
                    ) {
                        Text(text = "←")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onIntent(TasksIntent.CreateTaskClicked)
                },
            ) {
                Text(text = "+")
            }
        },
    ) { contentPadding ->
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
                    text = "Tarefas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "Acompanhe o que precisa ser feito neste espaço.",
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

                    state.isEmpty -> {
                        EmptyTasksContent(
                            onCreateTaskClick = {
                                onIntent(TasksIntent.CreateTaskClicked)
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> {
                        TasksListContent(
                            tasks = state.tasks,
                            onStatusClick = { task ->
                                onIntent(
                                    TasksIntent.TaskStatusClicked(
                                        taskId = task.id,
                                        currentStatus = task.status,
                                    ),
                                )
                            },
                            onDeleteClick = { task ->
                                onIntent(TasksIntent.DeleteTaskClicked(task.id))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MeSpaceTheme.spacing.large),
            )
        }
    }

    if (state.isCreateTaskDialogVisible) {
        CreateTaskDialog(
            title = state.newTaskTitle,
            description = state.newTaskDescription,
            selectedPriority = state.selectedPriority,
            isLoading = state.isCreatingTask,
            canConfirm = state.canCreateTask,
            errorMessage = state.createTaskErrorMessage,
            onTitleChanged = { title ->
                onIntent(TasksIntent.NewTaskTitleChanged(title))
            },
            onDescriptionChanged = { description ->
                onIntent(TasksIntent.NewTaskDescriptionChanged(description))
            },
            onPrioritySelected = { priority ->
                onIntent(TasksIntent.PrioritySelected(priority))
            },
            onDismiss = {
                onIntent(TasksIntent.CreateTaskDialogDismissed)
            },
            onConfirm = {
                onIntent(TasksIntent.CreateTaskConfirmed)
            },
        )
    }

    state.taskPendingDeletion?.let { taskPendingDeletion ->
        DeleteTaskConfirmationDialog(
            task = taskPendingDeletion,
            isDeleting = state.isPendingDeletionInProgress,
            onDismiss = {
                onIntent(TasksIntent.DeleteTaskDismissed)
            },
            onConfirm = {
                onIntent(TasksIntent.DeleteTaskConfirmed)
            },
        )
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
private fun EmptyTasksContent(
    onCreateTaskClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        MeSpaceEmptyState(
            title = "Nenhuma tarefa ainda",
            description = "Crie a primeira tarefa para organizar este espaço.",
            actionText = "Criar tarefa",
            onActionClick = onCreateTaskClick,
        )
    }
}

@Composable
private fun TasksListContent(
    tasks: List<TaskListItemUiModel>,
    onStatusClick: (TaskListItemUiModel) -> Unit,
    onDeleteClick: (TaskListItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            bottom = MeSpaceTheme.spacing.tripleExtraLarge + MeSpaceTheme.spacing.extraLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.medium),
    ) {
        items(
            items = tasks,
            key = { task -> task.id },
        ) { task ->
            TaskItem(
                task = task,
                onStatusClick = {
                    onStatusClick(task)
                },
                onDeleteClick = {
                    onDeleteClick(task)
                },
            )
        }
    }
}
