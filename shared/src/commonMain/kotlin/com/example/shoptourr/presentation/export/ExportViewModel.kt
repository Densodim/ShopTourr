package com.example.shoptourr.presentation.export

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateExportDraft
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.usecase.CreateExportUseCase
import com.example.shoptourr.domain.usecase.ObserveExportJobUseCase
import com.example.shoptourr.domain.usecase.ObservePremiumUseCase
import com.example.shoptourr.domain.usecase.RefreshExportJobUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ExportUiState(
    val tripId: String,
    val isLoading: Boolean = false,
    val isPolling: Boolean = false,
    val isPremium: Boolean = false,
    val format: ExportFormat = ExportFormat.PDF,
    val includeTaxFree: Boolean = true,
    val includeDiary: Boolean = false,
    val job: ExportJob? = null,
    val error: UiError? = null,
) : UiState

sealed interface ExportIntent {
    data class FormatChanged(val format: ExportFormat) : ExportIntent
    data class IncludeTaxFreeChanged(val value: Boolean) : ExportIntent
    data class IncludeDiaryChanged(val value: Boolean) : ExportIntent
    data object Create : ExportIntent
    data object Back : ExportIntent
}

sealed interface ExportUiEvent : UiEvent {
    data object NavigateBack : ExportUiEvent
    data object Logout : ExportUiEvent
}

class ExportViewModel(
    tripId: String,
    private val observeExportJob: ObserveExportJobUseCase,
    private val createExport: CreateExportUseCase,
    private val refreshExportJob: RefreshExportJobUseCase,
    private val observePremium: ObservePremiumUseCase,
    private val pollIntervalMs: Long = 1_000L,
) : BaseViewModel<ExportUiState, ExportUiEvent>(ExportUiState(tripId = tripId)) {

    private var pollJob: Job? = null
    private var isPremium: Boolean = false

    init {
        launch {
            observePremium().collectLatest { premium ->
                isPremium = premium
                updateState { copy(isPremium = premium) }
            }
        }
        launch {
            observeExportJob(state.value.tripId).collectLatest { job ->
                updateState { copy(job = job) }
                if (job?.isInProgress == true && pollJob?.isActive != true) {
                    startPolling(job.id)
                }
            }
        }
    }

    fun onIntent(intent: ExportIntent) {
        when (intent) {
            is ExportIntent.FormatChanged -> updateState { copy(format = intent.format, error = null) }
            is ExportIntent.IncludeTaxFreeChanged ->
                updateState { copy(includeTaxFree = intent.value, error = null) }
            is ExportIntent.IncludeDiaryChanged ->
                updateState { copy(includeDiary = intent.value, error = null) }
            ExportIntent.Create -> create()
            ExportIntent.Back -> emitEvent(ExportUiEvent.NavigateBack)
        }
    }

    private fun create() {
        launch {
            if (state.value.format == ExportFormat.PDF && !isPremium) {
                updateState {
                    copy(
                        isLoading = false,
                        error = AppError.Validation("premium").toUiError().copy(
                            title = "Нужен Premium",
                            message = "PDF-экспорт доступен на Plus или Pro",
                        ),
                    )
                }
                return@launch
            }
            updateState { copy(isLoading = true, error = null) }
            val draft = CreateExportDraft(
                format = state.value.format,
                includeTaxFree = state.value.includeTaxFree,
                includeDiary = state.value.includeDiary,
            )
            createExport(state.value.tripId, draft)
                .onSuccess { job ->
                    updateState { copy(isLoading = false, job = job) }
                    startPolling(job.id)
                }
                .onFailure { handleFailure(it) }
        }
    }

    private fun startPolling(exportId: String) {
        pollJob?.cancel()
        pollJob = launch {
            updateState { copy(isPolling = true) }
            while (isActive) {
                val result = refreshExportJob(exportId)
                val job = result.getOrElse {
                    handleFailure(it)
                    return@launch
                }
                updateState { copy(job = job) }
                if (job.isTerminal) {
                    updateState { copy(isPolling = false, isLoading = false) }
                    return@launch
                }
                delay(pollIntervalMs)
            }
        }
    }

    private fun handleFailure(throwable: Throwable) {
        val uiError = throwable.asAppError().toUiError()
        updateState { copy(isLoading = false, isPolling = false, error = uiError) }
        if (uiError.action is UiErrorAction.Logout) emitEvent(ExportUiEvent.Logout)
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
