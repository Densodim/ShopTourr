package com.example.shoptourr.presentation.base

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout

private data object LifecycleState : UiState

private sealed interface LifecycleEvent : UiEvent

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelLifecycleTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `onCleared cancels in-flight work`() = runTest {
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val vm = object : BaseViewModel<LifecycleState, LifecycleEvent>(LifecycleState) {
            init {
                launch {
                    try {
                        started.complete(Unit)
                        delay(Long.MAX_VALUE)
                    } finally {
                        cancelled.complete(Unit)
                    }
                }
            }
        }
        started.await()
        vm.onCleared()
        withTimeout(1_000) { cancelled.await() }
    }
}
