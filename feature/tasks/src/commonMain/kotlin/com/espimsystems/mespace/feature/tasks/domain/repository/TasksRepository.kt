package com.espimsystems.mespace.feature.tasks.domain.repository

import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    fun observeTasks(spaceId: String): Flow<AppResult<List<Task>>>

    suspend fun createTask(task: Task): AppResult<Task>

    suspend fun updateTaskStatus(
        spaceId: String,
        taskId: String,
        status: TaskStatus,
        updatedAtMillis: Long,
    ): AppResult<Task>

    suspend fun deleteTask(
        spaceId: String,
        taskId: String,
    ): AppResult<Unit>
}