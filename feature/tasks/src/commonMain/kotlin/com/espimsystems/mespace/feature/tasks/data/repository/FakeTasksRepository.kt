package com.espimsystems.mespace.feature.tasks.data.repository

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTasksRepository : TasksRepository {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())

    override fun observeTasks(spaceId: String): Flow<AppResult<List<Task>>> {
        return tasks.map { currentTasks ->
            AppResult.Success(
                currentTasks
                    .filter { task -> task.spaceId == spaceId }
                    .sortedForDisplay(),
            )
        }
    }

    override suspend fun createTask(task: Task): AppResult<Task> {
        val alreadyExists = tasks.value.any { existingTask ->
            existingTask.id == task.id
        }

        if (alreadyExists) {
            return AppResult.Failure(
                AppError.Validation(
                    field = FIELD_ID,
                    message = "Task already exists. id=${task.id}",
                ),
            )
        }

        tasks.update { currentTasks ->
            currentTasks + task
        }

        return AppResult.Success(task)
    }

    override suspend fun updateTaskStatus(
        spaceId: String,
        taskId: String,
        status: TaskStatus,
        updatedAtMillis: Long,
    ): AppResult<Task> {
        var updatedTask: Task? = null

        tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.spaceId == spaceId && task.id == taskId) {
                    val newTask = task.copy(
                        status = status,
                        updatedAtMillis = updatedAtMillis,
                        completedAtMillis = if (status == TaskStatus.DONE) {
                            updatedAtMillis
                        } else {
                            null
                        },
                    )

                    updatedTask = newTask
                    newTask
                } else {
                    task
                }
            }
        }

        return updatedTask
            ?.let { task ->
                AppResult.Success(task)
            }
            ?: AppResult.Failure(
                AppError.NotFound(
                    message = "Task not found. id=$taskId",
                ),
            )
    }

    override suspend fun deleteTask(
        spaceId: String,
        taskId: String,
    ): AppResult<Unit> {
        val exists = tasks.value.any { task ->
            task.spaceId == spaceId && task.id == taskId
        }

        if (!exists) {
            return AppResult.Failure(
                AppError.NotFound(
                    message = "Task not found. id=$taskId",
                ),
            )
        }

        tasks.update { currentTasks ->
            currentTasks.filterNot { task ->
                task.spaceId == spaceId && task.id == taskId
            }
        }

        return AppResult.Success(Unit)
    }

    private val TaskStatus.sortOrder: Int
        get() = when (this) {
            TaskStatus.IN_PROGRESS -> 0
            TaskStatus.PENDING -> 1
            TaskStatus.DONE -> 2
        }

    private val Task.prioritySortOrder: Int
        get() = when (priority) {
            TaskPriority.HIGH -> 0
            TaskPriority.MEDIUM -> 1
            TaskPriority.LOW -> 2
        }

    private fun List<Task>.sortedForDisplay(): List<Task> {
        return sortedWith(
            compareBy<Task> { task -> task.status.sortOrder }
                .thenBy { task -> task.prioritySortOrder }
                .thenByDescending { task -> task.updatedAtMillis.coerceAtLeast(task.createdAtMillis) }
                .thenBy { task -> task.id },
        )
    }

    private companion object {

        const val FIELD_ID = "id"
    }
}
