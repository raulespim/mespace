package com.espimsystems.mespace.feature.tasks.presentation

import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.mvi.MviComponent
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.session.UserSession
import com.espimsystems.mespace.core.logging.AppLogTags
import com.espimsystems.mespace.core.logging.AppLogger
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
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
        logger.debug(AppLogTags.TasksComponent) {
            "TasksComponent initialized. spaceId=$spaceId, spaceName=$spaceName"
        }

        observeTasks()
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
            TasksIntent.DeleteTaskDismissed -> handleDeleteTaskDismissed()
            TasksIntent.DeleteTaskConfirmed -> handleDeleteTaskConfirmed()
            TasksIntent.GeneralErrorConsumed -> handleGeneralErrorConsumed()
        }
    }

    private fun observeTasks() {
        updateState {
            copy(isLoading = true)
        }

        launchSafely(
            dispatcher = dispatchers.main,
            onError = { throwable ->
                logger.error(
                    tag = AppLogTags.TasksComponent,
                    throwable = throwable,
                ) {
                    "Failed to observe tasks. spaceId=$spaceId"
                }

                updateState {
                    copy(
                        isLoading = false,
                        generalErrorMessage = LOAD_TASKS_ERROR_MESSAGE,
                    )
                }
            },
        ) {
            observeTasksUseCase(spaceId).collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        updateState {
                            copy(
                                isLoading = false,
                                tasks = result.data.toUiModels(
                                    updatingTaskIds = updatingTaskIds,
                                    deletingTaskIds = deletingTaskIds,
                                ),
                                generalErrorMessage = null,
                            )
                        }
                    }

                    is AppResult.Failure -> {
                        logger.error(
                            tag = AppLogTags.TasksComponent,
                            throwable = result.error.cause,
                        ) {
                            "Failed to load tasks. spaceId=$spaceId"
                        }

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
                isCreatingTask = false,
                newTaskTitle = "",
                newTaskDescription = "",
                selectedPriority = TaskPriority.MEDIUM,
                createTaskErrorMessage = null,
                generalErrorMessage = null,
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
                createTaskErrorMessage = null,
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
                createTaskErrorMessage = null,
            )
        }
    }

    private fun handleNewTaskDescriptionChanged(
        intent: TasksIntent.NewTaskDescriptionChanged,
    ) {
        updateState {
            copy(
                newTaskDescription = intent.description,
                createTaskErrorMessage = null,
            )
        }
    }

    private fun handlePrioritySelected(
        intent: TasksIntent.PrioritySelected,
    ) {
        updateState {
            copy(
                selectedPriority = intent.priority,
                createTaskErrorMessage = null,
            )
        }
    }

    private fun handleCreateTaskConfirmed() {
        val currentState = state.value

        if (currentState.isCreatingTask) return

        if (currentState.newTaskTitle.isBlank()) {
            updateState {
                copy(createTaskErrorMessage = EMPTY_TASK_TITLE_MESSAGE)
            }
            return
        }

        updateState {
            copy(
                isCreatingTask = true,
                createTaskErrorMessage = null,
            )
        }

        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                logger.error(
                    tag = AppLogTags.TasksComponent,
                    throwable = throwable,
                ) {
                    "Unexpected error while creating task. spaceId=$spaceId"
                }

                updateState {
                    copy(
                        isCreatingTask = false,
                        createTaskErrorMessage = CREATE_TASK_ERROR_MESSAGE,
                    )
                }
            },
        ) {
            val result = createTaskUseCase(
                CreateTaskInput(
                    spaceId = spaceId,
                    title = currentState.newTaskTitle,
                    description = currentState.newTaskDescription,
                    priority = currentState.selectedPriority,
                    assignedToUserId = null,
                    createdByUserId = currentUser.userId,
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
                            createTaskErrorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    logger.error(
                        tag = AppLogTags.TasksComponent,
                        throwable = result.error.cause,
                    ) {
                        "Failed to create task. spaceId=$spaceId"
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
        val currentState = state.value
        val taskId = intent.taskId

        if (taskId in currentState.updatingTaskIds || taskId in currentState.deletingTaskIds) {
            return
        }

        val nextStatus = intent.currentStatus.next()
        val updatingTaskIds = currentState.updatingTaskIds + taskId

        updateState {
            copy(
                updatingTaskIds = updatingTaskIds,
                tasks = tasks.withOperationState(
                    updatingTaskIds = updatingTaskIds,
                    deletingTaskIds = deletingTaskIds,
                ),
                generalErrorMessage = null,
            )
        }

        logger.info(AppLogTags.TasksComponent) {
            "Updating task status. taskId=$taskId, from=${intent.currentStatus}, to=$nextStatus, spaceId=$spaceId"
        }

        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                logger.error(
                    tag = AppLogTags.TasksComponent,
                    throwable = throwable,
                ) {
                    "Unexpected error while updating task status. taskId=$taskId, spaceId=$spaceId"
                }

                finishTaskStatusUpdate(
                    taskId = taskId,
                    generalErrorMessage = UPDATE_TASK_ERROR_MESSAGE,
                )
            },
        ) {
            val result = updateTaskStatusUseCase(
                UpdateTaskStatusInput(
                    spaceId = spaceId,
                    taskId = taskId,
                    status = nextStatus,
                ),
            )

            when (result) {
                is AppResult.Success -> {
                    logger.info(AppLogTags.TasksComponent) {
                        "Task status updated. taskId=$taskId, status=${result.data.status}, spaceId=$spaceId"
                    }

                    finishTaskStatusUpdate(taskId = taskId)
                }

                is AppResult.Failure -> {
                    logger.error(
                        tag = AppLogTags.TasksComponent,
                        throwable = result.error.cause,
                    ) {
                        "Failed to update task status. taskId=$taskId, spaceId=$spaceId"
                    }

                    finishTaskStatusUpdate(
                        taskId = taskId,
                        generalErrorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    private fun handleDeleteTaskClicked(
        intent: TasksIntent.DeleteTaskClicked,
    ) {
        val currentState = state.value
        val taskId = intent.taskId

        if (taskId in currentState.updatingTaskIds || taskId in currentState.deletingTaskIds) {
            return
        }

        val task = currentState.tasks.firstOrNull { item -> item.id == taskId }
            ?: return

        updateState {
            copy(
                taskPendingDeletion = TaskPendingDeletionUiModel(
                    id = task.id,
                    title = task.title,
                ),
                generalErrorMessage = null,
            )
        }

        logger.info(AppLogTags.TasksComponent) {
            "Delete task confirmation opened. taskId=$taskId, spaceId=$spaceId"
        }
    }

    private fun handleDeleteTaskDismissed() {
        val pendingDeletion = state.value.taskPendingDeletion ?: return

        if (pendingDeletion.id in state.value.deletingTaskIds) return

        updateState {
            copy(taskPendingDeletion = null)
        }

        logger.debug(AppLogTags.TasksComponent) {
            "Delete task confirmation dismissed. taskId=${pendingDeletion.id}, spaceId=$spaceId"
        }
    }

    private fun handleDeleteTaskConfirmed() {
        val pendingDeletion = state.value.taskPendingDeletion ?: return
        val taskId = pendingDeletion.id

        if (taskId in state.value.deletingTaskIds) return

        val deletingTaskIds = state.value.deletingTaskIds + taskId

        updateState {
            copy(
                deletingTaskIds = deletingTaskIds,
                tasks = tasks.withOperationState(
                    updatingTaskIds = updatingTaskIds,
                    deletingTaskIds = deletingTaskIds,
                ),
                generalErrorMessage = null,
            )
        }

        logger.info(AppLogTags.TasksComponent) {
            "Delete task confirmed. taskId=$taskId, spaceId=$spaceId"
        }

        launchSafely(
            dispatcher = dispatchers.default,
            onError = { throwable ->
                logger.error(
                    tag = AppLogTags.TasksComponent,
                    throwable = throwable,
                ) {
                    "Unexpected error while deleting task. taskId=$taskId, spaceId=$spaceId"
                }

                finishTaskDeletion(
                    taskId = taskId,
                    clearPendingDeletion = true,
                    generalErrorMessage = DELETE_TASK_ERROR_MESSAGE,
                )
            },
        ) {
            when (val result = deleteTaskUseCase(spaceId = spaceId, taskId = taskId)) {
                is AppResult.Success -> {
                    logger.info(AppLogTags.TasksComponent) {
                        "Task deleted successfully. taskId=$taskId, spaceId=$spaceId"
                    }

                    finishTaskDeletion(
                        taskId = taskId,
                        clearPendingDeletion = true,
                    )
                }

                is AppResult.Failure -> {
                    logger.error(
                        tag = AppLogTags.TasksComponent,
                        throwable = result.error.cause,
                    ) {
                        "Failed to delete task. taskId=$taskId, spaceId=$spaceId"
                    }

                    finishTaskDeletion(
                        taskId = taskId,
                        clearPendingDeletion = true,
                        generalErrorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    private fun finishTaskStatusUpdate(
        taskId: String,
        generalErrorMessage: String? = null,
    ) {
        updateState {
            val updatedTaskIds = updatingTaskIds - taskId

            copy(
                updatingTaskIds = updatedTaskIds,
                tasks = tasks.withOperationState(
                    updatingTaskIds = updatedTaskIds,
                    deletingTaskIds = deletingTaskIds,
                ),
                generalErrorMessage = generalErrorMessage,
            )
        }
    }

    private fun finishTaskDeletion(
        taskId: String,
        clearPendingDeletion: Boolean,
        generalErrorMessage: String? = null,
    ) {
        updateState {
            val updatedDeletingTaskIds = deletingTaskIds - taskId

            copy(
                deletingTaskIds = updatedDeletingTaskIds,
                tasks = tasks.withOperationState(
                    updatingTaskIds = updatingTaskIds,
                    deletingTaskIds = updatedDeletingTaskIds,
                ),
                taskPendingDeletion = if (clearPendingDeletion) null else taskPendingDeletion,
                generalErrorMessage = generalErrorMessage,
            )
        }
    }

    private fun handleGeneralErrorConsumed() {
        updateState {
            copy(generalErrorMessage = null)
        }
    }

    private fun AppError.toUserMessage(): String {
        return when (this) {
            is AppError.Network -> "Verifique sua conexão e tente novamente."
            is AppError.Unauthorized -> "Entre novamente para continuar."
            is AppError.Forbidden -> "Você não tem permissão para fazer isso."
            is AppError.NotFound -> "Esta tarefa não foi encontrada."
            is AppError.Validation -> toValidationMessage()
            is AppError.Storage -> "Não foi possível salvar a tarefa agora."
            is AppError.Unknown -> "Algo deu errado. Tente novamente."
        }
    }

    private fun AppError.Validation.toValidationMessage(): String {
        return when (field) {
            FIELD_SPACE_ID -> "Não foi possível identificar o espaço."
            FIELD_TASK_ID -> "Não foi possível identificar a tarefa."
            FIELD_TITLE -> {
                if (message?.contains("longer") == true) {
                    "Use um título com até $TASK_TITLE_MAX_LENGTH caracteres."
                } else {
                    EMPTY_TASK_TITLE_MESSAGE
                }
            }
            FIELD_DESCRIPTION -> "Use uma descrição com até $TASK_DESCRIPTION_MAX_LENGTH caracteres."
            FIELD_CREATED_BY_USER_ID -> "Não foi possível identificar o usuário atual."
            FIELD_ID -> "Esta tarefa já existe."
            else -> "Confira as informações da tarefa e tente novamente."
        }
    }

    private companion object {

        const val FIELD_SPACE_ID = "spaceId"
        const val FIELD_TASK_ID = "taskId"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CREATED_BY_USER_ID = "createdByUserId"
        const val FIELD_ID = "id"

        const val TASK_TITLE_MAX_LENGTH = 80
        const val TASK_DESCRIPTION_MAX_LENGTH = 280

        const val EMPTY_TASK_TITLE_MESSAGE = "Informe um título para a tarefa."
        const val LOAD_TASKS_ERROR_MESSAGE = "Não foi possível carregar as tarefas. Tente novamente."
        const val CREATE_TASK_ERROR_MESSAGE = "Não foi possível criar a tarefa. Tente novamente."
        const val UPDATE_TASK_ERROR_MESSAGE = "Não foi possível atualizar a tarefa. Tente novamente."
        const val DELETE_TASK_ERROR_MESSAGE = "Não foi possível remover a tarefa. Tente novamente."
    }
}
