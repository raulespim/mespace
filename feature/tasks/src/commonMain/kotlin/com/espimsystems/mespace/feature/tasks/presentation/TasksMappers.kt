package com.espimsystems.mespace.feature.tasks.presentation

import com.espimsystems.mespace.feature.tasks.domain.model.Task

internal fun List<Task>.toUiModels(): List<TaskListItemUiModel> {
    return map { task ->
        task.toUiModel()
    }
}

private fun Task.toUiModel(): TaskListItemUiModel {
    return TaskListItemUiModel(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        assignedToLabel = assignedToUserId?.let { userId ->
            "Responsável: $userId"
        },
    )
}