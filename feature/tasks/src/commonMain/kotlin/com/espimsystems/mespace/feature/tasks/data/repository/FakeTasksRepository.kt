package com.espimsystems.mespace.feature.tasks.data.repository

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.Task
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
            val filteredTasks = currentTasks
                .asSequence()
                .filter { task -> task.spaceId == spaceId }
                .sortedWith(
                    compareBy<Task> { task -> task.status.sortOrder }
                        .thenByDescending { task -> task.createdAtMillis },
                )
                .toList()

            AppResult.Success(filteredTasks)
        }
    }

    override suspend fun createTask(task: Task): AppResult<Task> {
        val alreadyExists = tasks.value.any { existingTask ->
            existingTask.id == task.id
        }

        if (alreadyExists) {
            return AppResult.Failure(
                AppError.Validation(
                    message = "Task already exists. id=${task.id}",
                    cause = IllegalStateException()
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
            } ?: AppResult.Failure(
            AppError.NotFound(
                message = "Task not found. id=$taskId",
                cause = NoSuchElementException()
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
                    cause = NoSuchElementException()
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
            TaskStatus.PENDING -> 0
            TaskStatus.IN_PROGRESS -> 1
            TaskStatus.DONE -> 2
        }
}