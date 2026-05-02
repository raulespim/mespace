package com.espimsystems.mespace.feature.tasks.presentation

import com.espimsystems.mespace.feature.tasks.domain.model.Task

internal fun List<Task>.toUiModels(
    updatingTaskIds: Set<String> = emptySet(),
    deletingTaskIds: Set<String> = emptySet(),
): List<TaskListItemUiModel> {
    return map { task ->
        task.toUiModel(
            isUpdating = task.id in updatingTaskIds,
            isDeleting = task.id in deletingTaskIds,
        )
    }
}

internal fun List<TaskListItemUiModel>.withOperationState(
    updatingTaskIds: Set<String>,
    deletingTaskIds: Set<String>,
): List<TaskListItemUiModel> {
    return map { task ->
        task.copy(
            isUpdating = task.id in updatingTaskIds,
            isDeleting = task.id in deletingTaskIds,
        )
    }
}

private fun Task.toUiModel(
    isUpdating: Boolean,
    isDeleting: Boolean,
): TaskListItemUiModel {
    return TaskListItemUiModel(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        assignedToLabel = assignedToUserId?.let { userId ->
            "Responsável: $userId"
        },
        isUpdating = isUpdating,
        isDeleting = isDeleting,
    )
}
