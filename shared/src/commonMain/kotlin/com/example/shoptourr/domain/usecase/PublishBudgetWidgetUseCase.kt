package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.model.HomeSnapshot
import com.example.shoptourr.domain.widget.BudgetWidgetCopy
import com.example.shoptourr.domain.widget.BudgetWidgetRefresher
import com.example.shoptourr.domain.widget.BudgetWidgetStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PublishBudgetWidgetUseCase(
    private val observeHome: ObserveHomeUseCase,
    private val store: BudgetWidgetStore,
    private val refresher: BudgetWidgetRefresher,
) {
    suspend fun publish() {
        publish(observeHome().first())
    }

    fun start(scope: CoroutineScope) {
        scope.launch {
            observeHome().collect { home ->
                publish(home)
            }
        }
    }

    private fun publish(home: HomeSnapshot) {
        store.write(BudgetWidgetCopy.of(home))
        refresher.reload()
    }
}
