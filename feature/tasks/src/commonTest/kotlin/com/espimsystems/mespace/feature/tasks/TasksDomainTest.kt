package com.espimsystems.mespace.feature.tasks

import com.espimsystems.mespace.core.common.error.AppError
import com.espimsystems.mespace.core.common.id.IdGenerator
import com.espimsystems.mespace.core.common.result.AppResult
import com.espimsystems.mespace.core.common.time.ClockProvider
import com.espimsystems.mespace.feature.tasks.data.repository.FakeTasksRepository
import com.espimsystems.mespace.feature.tasks.domain.model.Task
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository
import com.espimsystems.mespace.feature.tasks.domain.usecase.CreateTaskInput
import com.espimsystems.mespace.feature.tasks.domain.usecase.CreateTaskUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.DeleteTaskUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.UpdateTaskStatusInput
import com.espimsystems.mespace.feature.tasks.domain.usecase.UpdateTaskStatusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class CreateTaskUseCaseTest {

    @Test
    fun invokeFailsWithBlankTitle() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = createTaskUseCase(repository = repository)

        val result = useCase(
            validCreateTaskInput(
                title = "   ",
            ),
        )

        val error = result.failureError()
        val validation = assertIs<AppError.Validation>(error)
        assertEquals("title", validation.field)
        assertNull(repository.createdTask)
    }

    @Test
    fun invokeFailsWithBlankSpaceId() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = createTaskUseCase(repository = repository)

        val result = useCase(
            validCreateTaskInput(
                spaceId = " ",
            ),
        )

        val error = result.failureError()
        val validation = assertIs<AppError.Validation>(error)
        assertEquals("spaceId", validation.field)
        assertNull(repository.createdTask)
    }

    @Test
    fun invokeFailsWithBlankCreatedByUserId() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = createTaskUseCase(repository = repository)

        val result = useCase(
            validCreateTaskInput(
                createdByUserId = "",
            ),
        )

        val error = result.failureError()
        val validation = assertIs<AppError.Validation>(error)
        assertEquals("createdByUserId", validation.field)
        assertNull(repository.createdTask)
    }

    @Test
    fun invokeNormalizesInputAndCreatesPendingTaskWithGeneratedIdAndTimestamps() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = createTaskUseCase(
            repository = repository,
            id = "generated-task-id",
            now = 1234L,
        )

        val result = useCase(
            validCreateTaskInput(
                title = "  Limpar    a cozinha  ",
                description = "  Antes do jantar  ",
                priority = TaskPriority.HIGH,
                assignedToUserId = "assigned-user-id",
            ),
        )

        val task = result.successData()
        assertEquals("generated-task-id", task.id)
        assertEquals("Limpar a cozinha", task.title)
        assertEquals("Antes do jantar", task.description)
        assertEquals(TaskStatus.PENDING, task.status)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals("assigned-user-id", task.assignedToUserId)
        assertEquals("demo-user-id", task.createdByUserId)
        assertEquals(1234L, task.createdAtMillis)
        assertEquals(1234L, task.updatedAtMillis)
        assertNull(task.completedAtMillis)
        assertEquals(task, repository.createdTask)
    }
}

class UpdateTaskStatusUseCaseTest {

    @Test
    fun invokeFailsWithBlankSpaceId() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = updateTaskStatusUseCase(repository = repository)

        val result = useCase(
            UpdateTaskStatusInput(
                spaceId = "",
                taskId = "task-id",
                status = TaskStatus.DONE,
            ),
        )

        val error = result.failureError()
        val validation = assertIs<AppError.Validation>(error)
        assertEquals("spaceId", validation.field)
        assertNull(repository.updateTaskStatusCall)
    }

    @Test
    fun invokeFailsWithBlankTaskId() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = updateTaskStatusUseCase(repository = repository)

        val result = useCase(
            UpdateTaskStatusInput(
                spaceId = "space-id",
                taskId = " ",
                status = TaskStatus.DONE,
            ),
        )

        val error = result.failureError()
        val validation = assertIs<AppError.Validation>(error)
        assertEquals("taskId", validation.field)
        assertNull(repository.updateTaskStatusCall)
    }

    @Test
    fun invokeUpdatesRepositoryWithClockTimestamp() = runBlockingTest {
        val repository = RecordingTasksRepository()
        val useCase = updateTaskStatusUseCase(
            repository = repository,
            now = 9876L,
        )

        val result = useCase(
            UpdateTaskStatusInput(
                spaceId = "space-id",
                taskId = "task-id",
                status = TaskStatus.DONE,
            ),
        )

        result.successData()
        val call = repository.updateTaskStatusCall ?: fail("Expected updateTaskStatus to be called.")
        assertEquals("space-id", call.spaceId)
        assertEquals("task-id", call.taskId)
        assertEquals(TaskStatus.DONE, call.status)
        assertEquals(9876L, call.updatedAtMillis)
    }
}

class DeleteTaskUseCaseTest {

    @Test
    fun invokeFailsWithBlankIds() = runBlockingTest {
        val repository = FakeTasksRepository()
        val useCase = DeleteTaskUseCase(repository)

        val blankSpaceResult = useCase(spaceId = "", taskId = "task-id")
        val blankSpaceValidation = assertIs<AppError.Validation>(blankSpaceResult.failureError())
        assertEquals("spaceId", blankSpaceValidation.field)

        val blankTaskResult = useCase(spaceId = "space-id", taskId = "")
        val blankTaskValidation = assertIs<AppError.Validation>(blankTaskResult.failureError())
        assertEquals("taskId", blankTaskValidation.field)
    }

    @Test
    fun invokeRemovesExistingTaskViaFakeRepository() = runBlockingTest {
        val repository = FakeTasksRepository()
        val useCase = DeleteTaskUseCase(repository)
        repository.createTask(sampleTask(id = "task-id", spaceId = "space-id")).successData()

        val result = useCase(spaceId = "space-id", taskId = "task-id")

        result.successData()
        val observedTasks = repository.observeTasks("space-id")
            .first()
            .successData()
        assertTrue(observedTasks.isEmpty())
    }
}

class FakeTasksRepositoryTest {

    @Test
    fun observeTasksFiltersBySpaceId() = runBlockingTest {
        val repository = FakeTasksRepository()
        repository.createTask(sampleTask(id = "task-a", spaceId = "space-a")).successData()
        repository.createTask(sampleTask(id = "task-b", spaceId = "space-b")).successData()

        val observedTasks = repository.observeTasks("space-a")
            .first()
            .successData()

        assertEquals(1, observedTasks.size)
        assertEquals("task-a", observedTasks.first().id)
    }

    @Test
    fun createTaskAddsTask() = runBlockingTest {
        val repository = FakeTasksRepository()

        val result = repository.createTask(sampleTask(id = "task-id", spaceId = "space-id"))

        assertEquals("task-id", result.successData().id)
        val observedTasks = repository.observeTasks("space-id")
            .first()
            .successData()
        assertEquals(listOf("task-id"), observedTasks.map { task -> task.id })
    }

    @Test
    fun createTaskWithDuplicatedIdReturnsValidation() = runBlockingTest {
        val repository = FakeTasksRepository()
        repository.createTask(sampleTask(id = "task-id", spaceId = "space-a")).successData()

        val result = repository.createTask(sampleTask(id = "task-id", spaceId = "space-b"))

        val error = result.failureError()
        val validation = assertIs<AppError.Validation>(error)
        assertEquals("id", validation.field)
    }

    @Test
    fun updateTaskStatusUpdatesStatusAndCompletedAtMillis() = runBlockingTest {
        val repository = FakeTasksRepository()
        repository.createTask(
            sampleTask(
                id = "task-id",
                spaceId = "space-id",
                status = TaskStatus.PENDING,
                updatedAtMillis = 10L,
            ),
        ).successData()

        val doneTask = repository.updateTaskStatus(
            spaceId = "space-id",
            taskId = "task-id",
            status = TaskStatus.DONE,
            updatedAtMillis = 20L,
        ).successData()

        assertEquals(TaskStatus.DONE, doneTask.status)
        assertEquals(20L, doneTask.updatedAtMillis)
        assertEquals(20L, doneTask.completedAtMillis)

        val reopenedTask = repository.updateTaskStatus(
            spaceId = "space-id",
            taskId = "task-id",
            status = TaskStatus.PENDING,
            updatedAtMillis = 30L,
        ).successData()

        assertEquals(TaskStatus.PENDING, reopenedTask.status)
        assertEquals(30L, reopenedTask.updatedAtMillis)
        assertNull(reopenedTask.completedAtMillis)
    }

    @Test
    fun deleteTaskRemovesByIdAndSpaceId() = runBlockingTest {
        val repository = FakeTasksRepository()
        repository.createTask(sampleTask(id = "task-id", spaceId = "space-a")).successData()

        val wrongSpaceResult = repository.deleteTask(spaceId = "space-b", taskId = "task-id")
        assertIs<AppError.NotFound>(wrongSpaceResult.failureError())

        val tasksBeforeCorrectDelete = repository.observeTasks("space-a")
            .first()
            .successData()
        assertEquals(1, tasksBeforeCorrectDelete.size)

        repository.deleteTask(spaceId = "space-a", taskId = "task-id").successData()

        val tasksAfterCorrectDelete = repository.observeTasks("space-a")
            .first()
            .successData()
        assertTrue(tasksAfterCorrectDelete.isEmpty())
    }

    @Test
    fun deleteMissingTaskReturnsNotFound() = runBlockingTest {
        val repository = FakeTasksRepository()

        val result = repository.deleteTask(spaceId = "space-id", taskId = "missing-task-id")

        assertIs<AppError.NotFound>(result.failureError())
    }
}

private fun createTaskUseCase(
    repository: TasksRepository,
    id: String = "task-id",
    now: Long = 1L,
): CreateTaskUseCase {
    return CreateTaskUseCase(
        repository = repository,
        idGenerator = FixedIdGenerator(id),
        clockProvider = FixedClockProvider(now),
    )
}

private fun updateTaskStatusUseCase(
    repository: TasksRepository,
    now: Long = 1L,
): UpdateTaskStatusUseCase {
    return UpdateTaskStatusUseCase(
        repository = repository,
        clockProvider = FixedClockProvider(now),
    )
}

private fun validCreateTaskInput(
    spaceId: String = "space-id",
    title: String = "Comprar mercado",
    description: String? = null,
    priority: TaskPriority = TaskPriority.MEDIUM,
    assignedToUserId: String? = null,
    createdByUserId: String = "demo-user-id",
): CreateTaskInput {
    return CreateTaskInput(
        spaceId = spaceId,
        title = title,
        description = description,
        priority = priority,
        assignedToUserId = assignedToUserId,
        createdByUserId = createdByUserId,
    )
}

private fun sampleTask(
    id: String = "task-id",
    spaceId: String = "space-id",
    title: String = "Tarefa",
    description: String? = null,
    status: TaskStatus = TaskStatus.PENDING,
    priority: TaskPriority = TaskPriority.MEDIUM,
    assignedToUserId: String? = null,
    createdByUserId: String = "demo-user-id",
    createdAtMillis: Long = 1L,
    updatedAtMillis: Long = createdAtMillis,
    completedAtMillis: Long? = null,
): Task {
    return Task(
        id = id,
        spaceId = spaceId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        assignedToUserId = assignedToUserId,
        createdByUserId = createdByUserId,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        completedAtMillis = completedAtMillis,
    )
}

private fun <T> AppResult<T>.successData(): T {
    return when (this) {
        is AppResult.Success -> data
        is AppResult.Failure -> fail("Expected AppResult.Success but was $error.")
    }
}

private fun <T> AppResult<T>.failureError(): AppError {
    return when (this) {
        is AppResult.Success -> fail("Expected AppResult.Failure but was $data.")
        is AppResult.Failure -> error
    }
}

private fun runBlockingTest(
    block: suspend () -> Unit,
) {
    runBlocking {
        block()
    }
}

private class FixedIdGenerator(
    private val id: String,
) : IdGenerator {

    override fun generateId(): String = id
}

private class FixedClockProvider(
    private val now: Long,
) : ClockProvider {

    override fun nowEpochMillis(): Long = now
}

private data class UpdateTaskStatusCall(
    val spaceId: String,
    val taskId: String,
    val status: TaskStatus,
    val updatedAtMillis: Long,
)

private class RecordingTasksRepository : TasksRepository {

    var createdTask: Task? = null
        private set

    var updateTaskStatusCall: UpdateTaskStatusCall? = null
        private set

    override fun observeTasks(spaceId: String): Flow<AppResult<List<Task>>> {
        return flowOf(AppResult.Success(emptyList()))
    }

    override suspend fun createTask(task: Task): AppResult<Task> {
        createdTask = task
        return AppResult.Success(task)
    }

    override suspend fun updateTaskStatus(
        spaceId: String,
        taskId: String,
        status: TaskStatus,
        updatedAtMillis: Long,
    ): AppResult<Task> {
        updateTaskStatusCall = UpdateTaskStatusCall(
            spaceId = spaceId,
            taskId = taskId,
            status = status,
            updatedAtMillis = updatedAtMillis,
        )

        return AppResult.Success(
            sampleTask(
                id = taskId,
                spaceId = spaceId,
                status = status,
                updatedAtMillis = updatedAtMillis,
                completedAtMillis = if (status == TaskStatus.DONE) updatedAtMillis else null,
            ),
        )
    }

    override suspend fun deleteTask(
        spaceId: String,
        taskId: String,
    ): AppResult<Unit> {
        return AppResult.Success(Unit)
    }
}
