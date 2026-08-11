package com.example.shoptourr.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface UiState
interface UiEvent

abstract class BaseViewModel<S : UiState, E : UiEvent>(
    initialState: S,
) : CoroutineScope {

    private val viewModelJob = SupervisorJob()
    override val coroutineContext = Dispatchers.Main.immediate + viewModelJob

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val events: SharedFlow<E> = _events.asSharedFlow()

    protected fun updateState(block: S.() -> S) {
        _state.update { it.block() }
    }

    protected fun emitEvent(event: E) {
        _events.tryEmit(event)
    }

    open fun onCleared() {
        cancel()
    }
}
