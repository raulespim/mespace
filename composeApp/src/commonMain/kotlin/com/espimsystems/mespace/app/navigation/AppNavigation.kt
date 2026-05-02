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
import com.espimsystems.mespace.core.logging.AppLogTags
import com.espimsystems.mespace.core.logging.AppLogger
import com.espimsystems.mespace.feature.spaces.data.repository.FakeSpacesRepository
import com.espimsystems.mespace.feature.spaces.domain.usecase.CreateSpaceUseCase
import com.espimsystems.mespace.feature.spaces.domain.usecase.ObserveSpacesUseCase
import com.espimsystems.mespace.feature.spaces.presentation.SpacesComponent
import com.espimsystems.mespace.feature.spaces.presentation.SpacesEffect
import com.espimsystems.mespace.feature.spaces.presentation.SpacesScreen
import com.espimsystems.mespace.feature.tasks.data.repository.FakeTasksRepository
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
    logger: AppLogger
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

    val spacesRepository = remember {
        FakeSpacesRepository()
    }

    val observeSpacesUseCase = remember {
        ObserveSpacesUseCase(
            spacesRepository = spacesRepository,
        )
    }

    val createSpaceUseCase = remember {
        CreateSpaceUseCase(
            spacesRepository = spacesRepository,
            idGenerator = UuidGenerator,
            clockProvider = SystemClockProvider,
        )
    }

    val tasksRepository = remember {
        FakeTasksRepository()
    }

    val observeTasksUseCase = remember {
        ObserveTasksUseCase(tasksRepository)
    }

    val createTaskUseCase = remember {
        CreateTaskUseCase(
            repository = tasksRepository,
            idGenerator = UuidGenerator,
            clockProvider = SystemClockProvider,
        )
    }

    val updateTaskStatusUseCase = remember {
        UpdateTaskStatusUseCase(
            repository = tasksRepository,
            clockProvider = SystemClockProvider,
        )
    }

    val deleteTaskUseCase = remember {
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
