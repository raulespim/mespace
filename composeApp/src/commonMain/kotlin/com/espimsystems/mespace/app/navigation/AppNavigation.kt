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
import com.espimsystems.mespace.features.spaces.data.repository.FakeSpacesRepository
import com.espimsystems.mespace.features.spaces.domain.usecase.CreateSpaceUseCase
import com.espimsystems.mespace.features.spaces.domain.usecase.ObserveSpacesUseCase
import com.espimsystems.mespace.features.spaces.presentation.SpacesComponent
import com.espimsystems.mespace.features.spaces.presentation.SpacesEffect
import com.espimsystems.mespace.features.spaces.presentation.SpacesScreen
import com.espimsystems.mespace.features.tasks.presentation.TasksPlaceholderScreen
import com.espimsystems.mespace.features.welcome.presentation.WelcomeComponent
import com.espimsystems.mespace.features.welcome.presentation.WelcomeEffect
import com.espimsystems.mespace.features.welcome.presentation.WelcomeScreen

@Composable
fun AppNavigation() {
    val navigator = remember { AppNavigator() }

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
                    )
                }

                is AppRoute.Tasks -> NavEntry(route) {
                    TasksPlaceholderScreen(
                        spaceName = route.spaceName,
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
) {
    val componentScope = rememberCoroutineScope()

    val component = remember {
        SpacesComponent(
            currentUser = currentUser,
            observeSpacesUseCase = observeSpacesUseCase,
            createSpaceUseCase = createSpaceUseCase,
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