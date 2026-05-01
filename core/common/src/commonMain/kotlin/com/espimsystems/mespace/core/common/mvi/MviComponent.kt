package com.espimsystems.mespace.core.common.mvi

import com.espimsystems.mespace.core.common.coroutines.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Multiplatform base
 */
abstract class MviComponent<STATE : UiState, INTENT : UiIntent, EFFECT : UiEffect>(
    initialState: STATE,
    protected val componentScope: CoroutineScope,
    protected val dispatchers: AppDispatchers,
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<EFFECT>(
        replay = 0,
        extraBufferCapacity = EFFECT_BUFFER_CAPACITY,
    )
    val effects: SharedFlow<EFFECT> = _effects.asSharedFlow()

    fun onIntent(intent: INTENT) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: INTENT)

    protected fun updateState(
        reducer: STATE.() -> STATE,
    ) {
        _state.update { currentState ->
            currentState.reducer()
        }
    }

    protected fun sendEffect(effect: EFFECT) {
        val emitted = _effects.tryEmit(effect)

        if (!emitted) {
            componentScope.launch {
                _effects.emit(effect)
            }
        }
    }

    protected fun launchSafely(
        dispatcher: CoroutineDispatcher = dispatchers.main,
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) {
        componentScope.launch(dispatcher) {
            try {
                block()
            } catch (throwable: Throwable) {
                onError(throwable)
            }
        }
    }

    private companion object {

        const val EFFECT_BUFFER_CAPACITY = 64
    }
}