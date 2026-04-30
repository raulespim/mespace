package com.espimsystems.mespace.features.welcome.presentation

import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import com.espimsystems.mespace.core.common.mvi.MviComponent
import com.espimsystems.mespace.core.common.mvi.UiEffect
import com.espimsystems.mespace.core.common.mvi.UiIntent
import com.espimsystems.mespace.core.common.mvi.UiState
import kotlinx.coroutines.CoroutineScope

data class WelcomeUiState(
    val appName: String = "MeSpace",
    val headline: String = "Organize tarefas com quem divide a rotina com você.",
    val primaryActionText: String = "Começar",
) : UiState

sealed interface WelcomeIntent : UiIntent {

    data object ContinueClicked : WelcomeIntent
}

sealed interface WelcomeEffect : UiEffect {

    data object NavigateToSpaces : WelcomeEffect
}

class WelcomeComponent(
    componentScope: CoroutineScope,
    dispatchers: AppDispatchers,
) : MviComponent<WelcomeUiState, WelcomeIntent, WelcomeEffect>(
    initialState = WelcomeUiState(),
    componentScope = componentScope,
    dispatchers = dispatchers,
) {

    override fun handleIntent(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.ContinueClicked -> handleContinueClicked()
        }
    }

    private fun handleContinueClicked() {
        sendEffect(WelcomeEffect.NavigateToSpaces)
    }
}