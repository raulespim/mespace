package com.espimsystems.mespace.feature.tasks.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espimsystems.mespace.feature.tasks.presentation.TasksIntent
import com.espimsystems.mespace.feature.tasks.presentation.TasksUiState
import com.espimsystems.mespace.feature.tasks.presentation.ui.CreateTaskDialog
import com.espimsystems.mespace.feature.tasks.presentation.ui.TaskItem

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

    Scaffold(
        modifier = modifier,
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.isEmpty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = "Nenhuma tarefa por aqui ainda.")
                    Text(text = "Crie a primeira responsabilidade deste espaço.")

                    Button(
                        onClick = {
                            onIntent(TasksIntent.CreateTaskClicked)
                        },
                    ) {
                        Text(text = "Criar tarefa")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.tasks,
                        key = { task -> task.id },
                    ) { task ->
                        TaskItem(
                            task = task,
                            onStatusClick = {
                                onIntent(
                                    TasksIntent.TaskStatusClicked(
                                        taskId = task.id,
                                        currentStatus = task.status,
                                    ),
                                )
                            },
                            onDeleteClick = {
                                onIntent(
                                    TasksIntent.DeleteTaskClicked(task.id),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    if (state.isCreateTaskDialogVisible) {
        CreateTaskDialog(
            title = state.newTaskTitle,
            description = state.newTaskDescription,
            selectedPriority = state.selectedPriority,
            isLoading = state.isCreatingTask,
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
}