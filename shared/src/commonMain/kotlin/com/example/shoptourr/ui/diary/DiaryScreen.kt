package com.example.shoptourr.ui.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.DiaryMoods
import com.example.shoptourr.presentation.diary.DiaryIntent
import com.example.shoptourr.presentation.diary.DiaryUiEvent
import com.example.shoptourr.presentation.diary.DiaryUiState
import com.example.shoptourr.presentation.diary.DiaryViewModel
import com.example.shoptourr.ui.components.EmptyState
import com.example.shoptourr.ui.components.LoadingBlock
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.components.VoyageButtonVariant
import com.example.shoptourr.ui.components.VoyageChip
import com.example.shoptourr.ui.components.VoyageScreen
import com.example.shoptourr.ui.components.VoyageSection
import com.example.shoptourr.ui.components.VoyageTextField
import com.example.shoptourr.ui.components.VoyageTopBar
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DiaryUiEvent.NavigateBack -> onBack()
                DiaryUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    DiaryContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun DiaryContent(
    state: DiaryUiState,
    onIntent: (DiaryIntent) -> Unit,
) {
    VoyageScreen {
        VoyageTopBar(title = t("diary"), onBack = { onIntent(DiaryIntent.Back) })
        Spacer(Modifier.height(12.dp))
        VoyageSection(title = t("add_entry")) {
            Text(t("mood"), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag(VoyageTestTags.DIARY_MOODS),
            ) {
                items(DiaryMoods.all, key = { it.id }) { mood ->
                    VoyageChip(
                        label = mood.glyph,
                        selected = state.moodDraft == mood.glyph,
                        onClick = { onIntent(DiaryIntent.MoodChanged(mood.glyph)) },
                        contentDescription = t(mood.labelKey),
                        modifier = Modifier.testTag("${VoyageTestTags.DIARY_MOOD}_${mood.id}"),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            VoyageTextField(
                value = state.textDraft,
                onValueChange = { onIntent(DiaryIntent.TextChanged(it)) },
                label = t("entry"),
                singleLine = false,
            )
            Spacer(Modifier.height(12.dp))
            VoyageButton(
                text = t("add"),
                onClick = { onIntent(DiaryIntent.Add) },
                isLoading = state.isSaving,
            )
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            UiErrorBanner(error = err)
        }
        Spacer(Modifier.height(20.dp))
        when {
            state.isLoading && state.days.isEmpty() -> LoadingBlock(label = t("loading"))
            state.days.isEmpty() -> EmptyState(
                title = t("diary_empty"),
                message = t("diary_empty_sub"),
            )
            else -> {
                state.days.forEach { day ->
                    Text(
                        text = day.date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    day.entries.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.mood,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(entry.text, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            VoyageButton(
                                text = t("delete"),
                                onClick = { onIntent(DiaryIntent.Delete(entry.id)) },
                                enabled = !state.isSaving,
                                variant = VoyageButtonVariant.Ghost,
                                fillMaxWidth = false,
                            )
                        }
                        HorizontalDivider()
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
