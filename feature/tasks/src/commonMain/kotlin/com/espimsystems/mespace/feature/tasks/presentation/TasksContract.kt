package com.espimsystems.mespace.feature.tasks.presentation

import com.espimsystems.mespace.core.common.mvi.UiEffect
import com.espimsystems.mespace.core.common.mvi.UiIntent
import com.espimsystems.mespace.core.common.mvi.UiState
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus

data class TasksUiState(
    val spaceId: String,
    val spaceName: String,
    val isLoading: Boolean = true,
    val tasks: List<TaskListItemUiModel> = emptyList(),
    val isCreateTaskDialogVisible: Boolean = false,
    val isCreatingTask: Boolean = false,
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val selectedPriority: TaskPriority = TaskPriority.MEDIUM,
    val createTaskErrorMessage: String? = null,
    val generalErrorMessage: String? = null,
    val updatingTaskIds: Set<String> = emptySet(),
    val deletingTaskIds: Set<String> = emptySet(),
    val taskPendingDeletion: TaskPendingDeletionUiModel? = null,
) : UiState {
    val isEmpty: Boolean
        get() = !isLoading && tasks.isEmpty()

    val canCreateTask: Boolean
        get() = newTaskTitle.isNotBlank() && !isCreatingTask

    val isPendingDeletionInProgress: Boolean
        get() = taskPendingDeletion
            ?.id
            ?.let { taskId -> taskId in deletingTaskIds }
            ?: false
}

data class TaskListItemUiModel(
    val id: String,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedToLabel: String?,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
)

data class TaskPendingDeletionUiModel(
    val id: String,
    val title: String,
)

sealed interface TasksIntent : UiIntent {

    data object BackClicked : TasksIntent

    data object CreateTaskClicked : TasksIntent

    data object CreateTaskDialogDismissed : TasksIntent

    data class NewTaskTitleChanged(
        val title: String,
    ) : TasksIntent

    data class NewTaskDescriptionChanged(
        val description: String,
    ) : TasksIntent

    data class PrioritySelected(
        val priority: TaskPriority,
    ) : TasksIntent

    data object CreateTaskConfirmed : TasksIntent

    data class TaskStatusClicked(
        val taskId: String,
        val currentStatus: TaskStatus,
    ) : TasksIntent

    data class DeleteTaskClicked(
        val taskId: String,
    ) : TasksIntent

    data object DeleteTaskDismissed : TasksIntent

    data object DeleteTaskConfirmed : TasksIntent

    data object GeneralErrorConsumed : TasksIntent
}

sealed interface TasksEffect : UiEffect {

    data object NavigateBack : TasksEffect
}
