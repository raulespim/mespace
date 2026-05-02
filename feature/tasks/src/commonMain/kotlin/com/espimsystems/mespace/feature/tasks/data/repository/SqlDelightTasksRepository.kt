package com.espimsystems.mespace.feature.tasks.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.database.MeSpaceDatabase
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightTasksRepository(
    private val database: MeSpaceDatabase,
    private val appDispatchers: AppDispatchers,
) : TasksRepository {

    override fun observeTasks(spaceId: String): Flow<AppResult<List<Task>>> {
        return database.tasksQueries
            .selectTasksBySpaceId(spaceId, ::mapTask)
            .asFlow()
            .mapToList(appDispatchers.io)
            .map<List<Task>, AppResult<List<Task>>> { tasks ->
                AppResult.Success(tasks)
            }
            .catch { throwable ->
                emit(storageFailure(throwable))
            }
    }

    override suspend fun createTask(task: Task): AppResult<Task> {
        return withContext(appDispatchers.io) {
            try {
                val alreadyExists = database.tasksQueries
                    .selectTaskByIdIgnoringSpace(task.id)
                    .executeAsOneOrNull() != null

                if (alreadyExists) {
                    AppResult.Failure(
                        AppError.Validation(
                            field = "id",
                            message = "Task already exists.",
                        ),
                    )
                } else {
                    database.tasksQueries.insertTask(
                        id = task.id,
                        spaceId = task.spaceId,
                        title = task.title,
                        description = task.description,
                        status = task.status.name,
                        priority = task.priority.name,
                        assignedToUserId = task.assignedToUserId,
                        createdByUserId = task.createdByUserId,
                        createdAtMillis = task.createdAtMillis,
                        updatedAtMillis = task.updatedAtMillis,
                        completedAtMillis = task.completedAtMillis,
                    )

                    AppResult.Success(task)
                }
            } catch (throwable: Throwable) {
                storageFailure(throwable)
            }
        }
    }

    override suspend fun updateTaskStatus(
        spaceId: String,
        taskId: String,
        status: TaskStatus,
        updatedAtMillis: Long,
    ): AppResult<Task> {
        return withContext(appDispatchers.io) {
            try {
                database.transactionWithResult {
                    val existingTask = database.tasksQueries
                        .selectTaskById(spaceId, taskId, ::mapTask)
                        .executeAsOneOrNull()

                    if (existingTask == null) {
                        AppResult.Failure(
                            AppError.NotFound(
                                message = "Task not found.",
                            ),
                        )
                    } else {
                        val completedAtMillis = if (status == TaskStatus.DONE) {
                            updatedAtMillis
                        } else {
                            null
                        }

                        database.tasksQueries.updateTaskStatus(
                            status = status.name,
                            updatedAtMillis = updatedAtMillis,
                            completedAtMillis = completedAtMillis,
                            spaceId = spaceId,
                            id = taskId,
                        )

                        val updatedTask = database.tasksQueries
                            .selectTaskById(spaceId, taskId, ::mapTask)
                            .executeAsOneOrNull()

                        if (updatedTask != null) {
                            AppResult.Success(updatedTask)
                        } else {
                            AppResult.Failure(
                                AppError.Storage(
                                    message = STORAGE_ERROR_MESSAGE,
                                ),
                            )
                        }
                    }
                }
            } catch (throwable: Throwable) {
                storageFailure(throwable)
            }
        }
    }

    override suspend fun deleteTask(
        spaceId: String,
        taskId: String,
    ): AppResult<Unit> {
        return withContext(appDispatchers.io) {
            try {
                database.transactionWithResult {
                    val exists = database.tasksQueries
                        .selectTaskById(spaceId, taskId)
                        .executeAsOneOrNull() != null

                    if (!exists) {
                        AppResult.Failure(
                            AppError.NotFound(
                                message = "Task not found.",
                            ),
                        )
                    } else {
                        database.tasksQueries.deleteTask(
                            spaceId = spaceId,
                            id = taskId,
                        )

                        AppResult.Success(Unit)
                    }
                }
            } catch (throwable: Throwable) {
                storageFailure(throwable)
            }
        }
    }

    private fun mapTask(
        id: String,
        spaceId: String,
        title: String,
        description: String?,
        status: String,
        priority: String,
        assignedToUserId: String?,
        createdByUserId: String,
        createdAtMillis: Long,
        updatedAtMillis: Long,
        completedAtMillis: Long?,
    ): Task {
        return Task(
            id = id,
            spaceId = spaceId,
            title = title,
            description = description,
            status = TaskStatus.valueOf(status),
            priority = TaskPriority.valueOf(priority),
            assignedToUserId = assignedToUserId,
            createdByUserId = createdByUserId,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
            completedAtMillis = completedAtMillis,
        )
    }

    private fun storageFailure(throwable: Throwable): AppResult.Failure {
        return AppResult.Failure(
            AppError.Storage(
                message = STORAGE_ERROR_MESSAGE,
                cause = throwable,
            ),
        )
    }

    private companion object {

        const val STORAGE_ERROR_MESSAGE = "Unable to access local database."
    }
}
