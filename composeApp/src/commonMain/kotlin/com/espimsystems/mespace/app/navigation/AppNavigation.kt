package com.espimsystems.mespace.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.espimsystems.mespace.core.common.coroutines.DefaultAppDispatchers
import com.espimsystems.mespace.core.common.id.UuidGenerator
import com.espimsystems.mespace.core.common.session.UserSession
import com.espimsystems.mespace.core.common.time.SystemClockProvider
import com.espimsystems.mespace.core.database.MeSpaceDatabase
import com.espimsystems.mespace.core.logging.AppLogTags
import com.espimsystems.mespace.core.logging.AppLogger
import com.espimsystems.mespace.feature.spaces.data.repository.FakeSpacesRepository
import com.espimsystems.mespace.feature.spaces.data.repository.SqlDelightSpacesRepository
import com.espimsystems.mespace.feature.spaces.domain.repository.SpacesRepository
import com.espimsystems.mespace.feature.spaces.domain.usecase.CreateSpaceUseCase
import com.espimsystems.mespace.feature.spaces.domain.usecase.ObserveSpacesUseCase
import com.espimsystems.mespace.feature.spaces.presentation.SpacesComponent
import com.espimsystems.mespace.feature.spaces.presentation.SpacesEffect
import com.espimsystems.mespace.feature.spaces.presentation.SpacesScreen
import com.espimsystems.mespace.feature.tasks.data.repository.FakeTasksRepository
import com.espimsystems.mespace.feature.tasks.data.repository.SqlDelightTasksRepository
import com.espimsystems.mespace.feature.tasks.domain.repository.TasksRepository
import com.espimsystems.mespace.feature.tasks.domain.usecase.CreateTaskUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.DeleteTaskUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.ObserveTasksUseCase
import com.espimsystems.mespace.feature.tasks.domain.usecase.UpdateTaskStatusUseCase
import com.espimsystems.mespace.feature.tasks.presentation.TasksComponent
import com.espimsystems.mespace.feature.tasks.presentation.TasksEffect
import com.espimsystems.mespace.feature.tasks.presentation.ui.screens.TasksScreen
import com.espimsystems.mespace.features.welcome.presentation.WelcomeComponent
import com.espimsystems.mespace.features.welcome.presentation.WelcomeEffect
import com.espimsystems.mespace.features.welcome.presentation.WelcomeScreen

@Composable
fun AppNavigation(
    logger: AppLogger,
    database: MeSpaceDatabase,
) {
    val spacesRepository = remember(database) {
        SqlDelightSpacesRepository(
            database = database,
            appDispatchers = DefaultAppDispatchers,
        )
    }

    val tasksRepository = remember(database) {
        SqlDelightTasksRepository(
            database = database,
            appDispatchers = DefaultAppDispatchers,
        )
    }

    AppNavigationContent(
        logger = logger,
        spacesRepository = spacesRepository,
        tasksRepository = tasksRepository,
    )
}

@Composable
fun PreviewAppNavigation(
    logger: AppLogger,
) {
    val spacesRepository = remember {
        FakeSpacesRepository()
    }

    val tasksRepository = remember {
        FakeTasksRepository()
    }

    AppNavigationContent(
        logger = logger,
        spacesRepository = spacesRepository,
        tasksRepository = tasksRepository,
    )
}

@Composable
private fun AppNavigationContent(
    logger: AppLogger,
    spacesRepository: SpacesRepository,
    tasksRepository: TasksRepository,
) {
    val navigator = remember { AppNavigator() }

    LaunchedEffect(Unit) {
        logger.debug(AppLogTags.Navigation) {
            "AppNavigation started."
        }
    }

    val currentUser = remember {
        UserSession(
            userId = "demo-user-id",
            displayName = "Demo User",
            email = "demo@mespace.app",
        )
    }

    val observeSpacesUseCase = remember(spacesRepository) {
        ObserveSpacesUseCase(
            spacesRepository = spacesRepository,
        )
    }

    val createSpaceUseCase = remember(spacesRepository) {
        CreateSpaceUseCase(
            spacesRepository = spacesRepository,
            idGenerator = UuidGenerator,
            clockProvider = SystemClockProvider,
        )
    }

    val observeTasksUseCase = remember(tasksRepository) {
        ObserveTasksUseCase(tasksRepository)
    }

    val createTaskUseCase = remember(tasksRepository) {
        CreateTaskUseCase(
            repository = tasksRepository,
            idGenerator = UuidGenerator,
            clockProvider = SystemClockProvider,
        )
    }

    val updateTaskStatusUseCase = remember(tasksRepository) {
        UpdateTaskStatusUseCase(
            repository = tasksRepository,
            clockProvider = SystemClockProvider,
        )
    }

    val deleteTaskUseCase = remember(tasksRepository) {
        DeleteTaskUseCase(tasksRepository)
    }

    NavDisplay(
        backStack = navigator.backStack,
        onBack = {
            navigator.navigateBack()
        },
        entryProvider = { route ->
            when (route) {
                AppRoute.Welcome -> NavEntry(route) {
                    WelcomeRoute(
                        navigator = navigator,
                    )
                }

                AppRoute.Spaces -> NavEntry(route) {
                    SpacesRoute(
                        navigator = navigator,
                        currentUser = currentUser,
                        observeSpacesUseCase = observeSpacesUseCase,
                        createSpaceUseCase = createSpaceUseCase,
                        logger = logger,
                    )
                }

                is AppRoute.Tasks -> NavEntry(route) {
                    TasksRoute(
                        navigator = navigator,
                        spaceId = route.spaceId,
                        spaceName = route.spaceName,
                        currentUser = currentUser,
                        observeTasksUseCase = observeTasksUseCase,
                        createTaskUseCase = createTaskUseCase,
                        updateTaskStatusUseCase = updateTaskStatusUseCase,
                        deleteTaskUseCase = deleteTaskUseCase,
                        logger = logger,
                    )
                }
            }
        },
    )
}

@Composable
private fun WelcomeRoute(
    navigator: AppNavigator,
) {
    val componentScope = rememberCoroutineScope()

    val component = remember {
        WelcomeComponent(
            componentScope = componentScope,
            dispatchers = DefaultAppDispatchers,
        )
    }

    val state by component.state.collectAsState()

    LaunchedEffect(component) {
        component.effects.collect { effect ->
            when (effect) {
                WelcomeEffect.NavigateToSpaces -> navigator.navigateToSpaces()
            }
        }
    }

    WelcomeScreen(
        state = state,
        onIntent = component::onIntent,
    )
}

@Composable
private fun SpacesRoute(
    navigator: AppNavigator,
    currentUser: UserSession,
    observeSpacesUseCase: ObserveSpacesUseCase,
    createSpaceUseCase: CreateSpaceUseCase,
    logger: AppLogger
) {
    val componentScope = rememberCoroutineScope()

    val component = remember {
        SpacesComponent(
            currentUser = currentUser,
            observeSpacesUseCase = observeSpacesUseCase,
            createSpaceUseCase = createSpaceUseCase,
            logger = logger,
            componentScope = componentScope,
            dispatchers = DefaultAppDispatchers,
        )
    }

    val state by component.state.collectAsState()

    LaunchedEffect(component) {
        component.effects.collect { effect ->
            when (effect) {
                is SpacesEffect.NavigateToTasks -> {
                    navigator.navigateToTasks(
                        spaceId = effect.spaceId,
                        spaceName = effect.spaceName,
                    )
                }
            }
        }
    }

    SpacesScreen(
        state = state,
        onIntent = component::onIntent,
    )
}

@Composable
private fun TasksRoute(
    navigator: AppNavigator,
    spaceId: String,
    spaceName: String,
    currentUser: UserSession,
    observeTasksUseCase: ObserveTasksUseCase,
    createTaskUseCase: CreateTaskUseCase,
    updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    deleteTaskUseCase: DeleteTaskUseCase,
    logger: AppLogger,
) {
    val componentScope = rememberCoroutineScope()

    val component = remember(
        spaceId,
        spaceName,
    ) {
        TasksComponent(
            currentUser = currentUser,
            spaceId = spaceId,
            spaceName = spaceName,
            observeTasksUseCase = observeTasksUseCase,
            createTaskUseCase = createTaskUseCase,
            updateTaskStatusUseCase = updateTaskStatusUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            logger = logger,
            componentScope = componentScope,
            dispatchers = DefaultAppDispatchers
        )
    }

    val state by component.state.collectAsState()

    LaunchedEffect(component) {
        component.effects.collect { effect ->
            when (effect) {
                TasksEffect.NavigateBack -> {
                    navigator.navigateBack()
                }
            }
        }
    }

    TasksScreen(
        state = state,
        onIntent = component::onIntent,
    )
}
