package com.example.shoptourr.presentation.diary

import com.example.shoptourr.domain.error.asAppError
import com.example.shoptourr.domain.model.CreateDiaryDraft
import com.example.shoptourr.domain.model.DiaryDayGroup
import com.example.shoptourr.domain.usecase.CreateDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.DeleteDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.ObserveDiaryUseCase
import com.example.shoptourr.domain.usecase.RefreshDiaryUseCase
import com.example.shoptourr.presentation.base.BaseViewModel
import com.example.shoptourr.presentation.base.UiEvent
import com.example.shoptourr.presentation.base.UiState
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DiaryUiState(
    val tripId: String,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val days: List<DiaryDayGroup> = emptyList(),
    val moodDraft: String = "good",
    val textDraft: String = "",
    val error: UiError? = null,
) : UiState

sealed interface DiaryIntent {
    data object Refresh : DiaryIntent
    data class MoodChanged(val value: String) : DiaryIntent
    data class TextChanged(val value: String) : DiaryIntent
    data object Add : DiaryIntent
    data class Delete(val entryId: String) : DiaryIntent
    data object Back : DiaryIntent
}

sealed interface DiaryUiEvent : UiEvent {
    data object NavigateBack : DiaryUiEvent
    data object Logout : DiaryUiEvent
}

class DiaryViewModel(
    tripId: String,
    private val observeDiary: ObserveDiaryUseCase,
    private val refreshDiary: RefreshDiaryUseCase,
    private val createEntry: CreateDiaryEntryUseCase,
    private val deleteEntry: DeleteDiaryEntryUseCase,
) : BaseViewModel<DiaryUiState, DiaryUiEvent>(DiaryUiState(tripId = tripId)) {

    init {
        launch {
            observeDiary(state.value.tripId).collectLatest { days ->
                updateState { copy(days = days, isLoading = false) }
            }
        }
        onIntent(DiaryIntent.Refresh)
    }

    fun onIntent(intent: DiaryIntent) {
        when (intent) {
            DiaryIntent.Refresh -> refresh()
            is DiaryIntent.MoodChanged -> updateState { copy(moodDraft = intent.value, error = null) }
            is DiaryIntent.TextChanged -> updateState { copy(textDraft = intent.value, error = null) }
            DiaryIntent.Add -> add()
            is DiaryIntent.Delete -> remove(intent.entryId)
            DiaryIntent.Back -> emitEvent(DiaryUiEvent.NavigateBack)
        }
    }

    private fun refresh() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            refreshDiary(state.value.tripId)
                .onSuccess { updateState { copy(isLoading = false) } }
                .onFailure { handleFailure(it) }
        }
    }

    private fun add() {
        launch {
            val current = state.value
            updateState { copy(isSaving = true, error = null) }
            createEntry(
                current.tripId,
                CreateDiaryDraft(mood = current.moodDraft, text = current.textDraft),
            )
                .onSuccess {
                    updateState { copy(isSaving = false, textDraft = "", error = null) }
                }
                .onFailure { handleFailure(it, saving = true) }
        }
    }

    private fun remove(entryId: String) {
        launch {
            updateState { copy(isSaving = true, error = null) }
            deleteEntry(state.value.tripId, entryId)
                .onSuccess { updateState { copy(isSaving = false) } }
                .onFailure { handleFailure(it, saving = true) }
        }
    }

    private fun handleFailure(throwable: Throwable, saving: Boolean = false) {
        val uiError = throwable.asAppError().toUiError()
        updateState {
            copy(
                isLoading = false,
                isSaving = if (saving) false else isSaving,
                error = uiError,
            )
        }
        if (uiError.action is UiErrorAction.Logout) emitEvent(DiaryUiEvent.Logout)
    }
}
