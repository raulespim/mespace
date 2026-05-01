package com.espimsystems.mespace.feature.tasks.presentation

import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.id.IdGenerator
import com.espimsystems.mespace.core.common.mvi.MviComponent
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.session.UserSession
import com.espimsystems.mespace.core.common.time.ClockProvider
import com.espimsystems.mespace.core.logging.AppLogTags
import com.espimsystems.mespace.core.logging.AppLogger
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.usecase.CreateTaskInput
import com.espimsystems.mespace.feature.tasks.domain.usecase.CreateTaskUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.DeleteTaskUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.ObserveTasksUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.UpdateTaskStatusInput
import com.espimsystems.mespace.feature.tasks.domain.usecase.UpdateTaskStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest

class TasksComponent(
    private val currentUser: UserSession,
    private val spaceId: String,
    private val spaceName: String,
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val idGenerator: IdGenerator,
    private val clockProvider: ClockProvider,
    private val logger: AppLogger,
    componentScope: CoroutineScope,
    dispatchers: AppDispatchers,
) : MviComponent<TasksUiState, TasksIntent, TasksEffect>(
    initialState = TasksUiState(
        spaceId = spaceId,
        spaceName = spaceName,
    ),
    componentScope = componentScope,
    dispatchers = dispatchers,
) {

    init {
        observeTasks()

        logger.debug(AppLogTags.TasksComponent) {
            "TasksComponent initialized. spaceId=$spaceId, spaceName=$spaceName"
        }
    }

    override fun handleIntent(intent: TasksIntent) {
        when (intent) {
            TasksIntent.BackClicked -> handleBackClicked()
            TasksIntent.CreateTaskClicked -> handleCreateTaskClicked()
            TasksIntent.CreateTaskDialogDismissed -> handleCreateTaskDialogDismissed()
            is TasksIntent.NewTaskTitleChanged -> handleNewTaskTitleChanged(intent)
            is TasksIntent.NewTaskDescriptionChanged -> handleNewTaskDescriptionChanged(intent)
            is TasksIntent.PrioritySelected -> handlePrioritySelected(intent)
            TasksIntent.CreateTaskConfirmed -> handleCreateTaskConfirmed()
            is TasksIntent.TaskStatusClicked -> handleTaskStatusClicked(intent)
            is TasksIntent.DeleteTaskClicked -> handleDeleteTaskClicked(intent)
            TasksIntent.GeneralErrorConsumed -> handleErrorMessageShown()
        }
    }

    private fun observeTasks() {
        updateState {
            copy(isLoading = true)
        }

        launchSafely(
            dispatcher = dispatchers.main,
            onError = { throwable ->
                updateState {
                    copy(
                        isLoading = false,
                        generalErrorMessage = throwable.message ?: "Unable to load tasks.",
                    )
                }
            },
        ) {
            observeTasksUseCase(spaceId).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        logger.debug(AppLogTags.TasksComponent) {
                            "Tasks updated. count=${result.data.size}, spaceId=$spaceId"
                        }

                        updateState {
                            copy(
                                isLoading = false,
                                tasks = result.data.toUiModels(),
                                generalErrorMessage = null,
                            )
                        }
                    }

                    is AppResult.Failure -> {
                        updateState {
                            copy(
                                isLoading = false,
                                generalErrorMessage = result.error.toUserMessage(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleBackClicked() {
        sendEffect(TasksEffect.NavigateBack)
    }

    private fun handleCreateTaskClicked() {
        updateState {
            copy(
                isCreateTaskDialogVisible = true,
                newTaskTitle = "",
                newTaskDescription = "",
                selectedPriority = TaskPriority.MEDIUM,
                createTaskErrorMessage = null,
                generalErrorMessage = null
            )
        }

        logger.debug(AppLogTags.TasksComponent) {
            "Create task dialog opened. spaceId=$spaceId"
        }
    }

    private fun handleCreateTaskDialogDismissed() {
        if (state.value.isCreatingTask) return

        updateState {
            copy(
                isCreateTaskDialogVisible = false,
                newTaskTitle = "",
                newTaskDescription = "",
                selectedPriority = TaskPriority.MEDIUM,
                generalErrorMessage = null,
            )
        }

        logger.debug(AppLogTags.TasksComponent) {
            "Create task dialog dismissed. spaceId=$spaceId"
        }
    }

    private fun handleNewTaskTitleChanged(
        intent: TasksIntent.NewTaskTitleChanged,
    ) {
        updateState {
            copy(
                newTaskTitle = intent.title,
                generalErrorMessage = null,
            )
        }
    }

    private fun handleNewTaskDescriptionChanged(
        intent: TasksIntent.NewTaskDescriptionChanged,
    ) {
        updateState {
            copy(newTaskDescription = intent.description)
        }
    }

    private fun handlePrioritySelected(
        intent: TasksIntent.PrioritySelected,
    ) {
        updateState {
            copy(selectedPriority = intent.priority)
        }
    }

    private fun handleCreateTaskConfirmed() {
        val currentState = state.value

        if (!currentState.canCreateTask) return

        updateState {
            copy(
                isCreatingTask = true,
                createTaskErrorMessage = null,
            )
        }

        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                updateState {
                    copy(
                        isCreatingTask = false,
                        createTaskErrorMessage = throwable.message ?: "Unable to create task.",
                    )
                }
            },
        ) {
            val now = clockProvider.nowEpochMillis()

            val result = createTaskUseCase(
                CreateTaskInput(
                    id = idGenerator.generateId(),
                    spaceId = spaceId,
                    title = currentState.newTaskTitle,
                    description = currentState.newTaskDescription,
                    priority = currentState.selectedPriority,
                    assignedToUserId = null,
                    createdByUserId = currentUser.userId,
                    createdAtMillis = now,
                ),
            )

            when (result) {
                is AppResult.Success -> {
                    logger.info(AppLogTags.TasksComponent) {
                        "Task created successfully. taskId=${result.data.id}, spaceId=$spaceId"
                    }

                    updateState {
                        copy(
                            isCreatingTask = false,
                            isCreateTaskDialogVisible = false,
                            newTaskTitle = "",
                            newTaskDescription = "",
                            selectedPriority = TaskPriority.MEDIUM,
                            generalErrorMessage = null,
                            createTaskErrorMessage = null
                        )
                    }
                }

                is AppResult.Failure -> {
                    logger.error(
                        tag = AppLogTags.TasksComponent,
                        throwable = result.error.cause,
                    ) {
                        "Failed to create task. error=${result.error.message}, spaceId=$spaceId"
                    }

                    updateState {
                        copy(
                            isCreatingTask = false,
                            createTaskErrorMessage = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun handleTaskStatusClicked(
        intent: TasksIntent.TaskStatusClicked,
    ) {
        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                updateState {
                    copy(
                        generalErrorMessage = throwable.message ?: "Unable to update task.",
                    )
                }
            },
        ) {
            val nextStatus = intent.currentStatus.next()
            val now = clockProvider.nowEpochMillis()

            logger.info(AppLogTags.TasksComponent) {
                "Updating task status. taskId=${intent.taskId}, from=${intent.currentStatus}, to=$nextStatus"
            }

            val result = updateTaskStatusUseCase(
                UpdateTaskStatusInput(
                    spaceId = spaceId,
                    taskId = intent.taskId,
                    status = nextStatus,
                    updatedAtMillis = now,
                ),
            )

            when (result) {
                is AppResult.Success -> {
                    updateState {
                        copy(generalErrorMessage = null)
                    }
                }

                is AppResult.Failure -> {
                    logger.error(
                        tag = AppLogTags.TasksComponent,
                        throwable = result.error.cause,
                    ) {
                        "Failed to update task status. taskId=${intent.taskId}"
                    }

                    updateState {
                        copy(generalErrorMessage = result.error.toUserMessage())
                    }
                }
            }
        }
    }

    private fun handleDeleteTaskClicked(
        intent: TasksIntent.DeleteTaskClicked,
    ) {
        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                updateState {
                    copy(
                        generalErrorMessage = throwable.message ?: "Unable to delete task.",
                    )
                }
            },
        ) {
            logger.info(AppLogTags.TasksComponent) {
                "Deleting task. taskId=${intent.taskId}, spaceId=$spaceId"
            }

            val result = deleteTaskUseCase(
                spaceId = spaceId,
                taskId = intent.taskId,
            )

            when (result) {
                is AppResult.Success -> {
                    updateState {
                        copy(generalErrorMessage = null)
                    }
                }

                is AppResult.Failure -> {
                    logger.error(
                        tag = AppLogTags.TasksComponent,
                        throwable = result.error.cause,
                    ) {
                        "Failed to delete task. taskId=${intent.taskId}"
                    }

                    updateState {
                        copy(generalErrorMessage = result.error.toUserMessage())
                    }
                }
            }
        }
    }

    private fun handleErrorMessageShown() {
        updateState {
            copy(generalErrorMessage = null, createTaskErrorMessage = null)
        }
    }

    private fun AppError.toUserMessage(): String {
        return when (this) {
            is AppError.Network -> "Check your internet connection and try again."
            is AppError.Unauthorized -> "You need to sign in again."
            is AppError.Forbidden -> "You do not have permission to do this."
            is AppError.NotFound -> "This task was not found."
            is AppError.Validation -> message ?: "Please check the information and try again."
            is AppError.Storage -> "Unable to save your data right now."
            is AppError.Unknown -> "Something went wrong. Please try again."
        }
    }
}