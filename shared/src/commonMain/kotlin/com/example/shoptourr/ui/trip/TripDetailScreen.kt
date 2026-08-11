package com.example.shoptourr.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.trip.TripDetailIntent
import com.example.shoptourr.presentation.trip.TripDetailUiEvent
import com.example.shoptourr.presentation.trip.TripDetailViewModel

@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    onAddPurchase: (tripId: String) -> Unit,
    onOpenDiary: (tripId: String) -> Unit = {},
    onOpenTaxFree: (tripId: String) -> Unit = {},
    onOpenAlerts: (tripId: String) -> Unit = {},
    onOpenMap: (tripId: String) -> Unit = {},
    onOpenStats: (tripId: String) -> Unit = {},
    onOpenExport: (tripId: String) -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TripDetailUiEvent.NavigateAddPurchase -> onAddPurchase(event.tripId)
                is TripDetailUiEvent.NavigateDiary -> onOpenDiary(event.tripId)
                is TripDetailUiEvent.NavigateTaxFree -> onOpenTaxFree(event.tripId)
                is TripDetailUiEvent.NavigateAlerts -> onOpenAlerts(event.tripId)
                is TripDetailUiEvent.NavigateMap -> onOpenMap(event.tripId)
                is TripDetailUiEvent.NavigateStats -> onOpenStats(event.tripId)
                is TripDetailUiEvent.NavigateExport -> onOpenExport(event.tripId)
                TripDetailUiEvent.NavigateBack -> onBack()
                TripDetailUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        TextButton(onClick = { viewModel.onIntent(TripDetailIntent.Back) }) {
            Text("Назад")
        }
        if (state.isLoading && state.detail == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            return
        }
        val detail = state.detail
        if (detail == null) {
            Text(
                text = state.error?.title ?: "Not Found",
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = state.error?.message.orEmpty(),
                color = MaterialTheme.colorScheme.error,
            )
            return
        }

        Text(
            text = detail.trip.city,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = detail.trip.country,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${detail.trip.startDate} — ${detail.trip.endDate}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Бюджет: ${detail.trip.budget.toDecimalString()} ${detail.trip.budget.currency}",
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Потрачено: ${detail.spentTotal.toDecimalString()} ${detail.spentTotal.currency}",
            color = MaterialTheme.colorScheme.onBackground,
        )
        detail.trip.exchangeRate?.let { fx ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "FX: 1 ${fx.tripCurrency} = ${fx.rate} ${fx.quoteCurrency}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { viewModel.onIntent(TripDetailIntent.RefreshFx) }) {
                Text("Обновить курс")
            }
        }
        if (detail.trip.travelers.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Участники", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            detail.trip.travelers.forEach { traveler ->
                Text(
                    text = "${traveler.avatarGlyph} ${traveler.name}" + if (traveler.isOwner) " · owner" else "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.travelerNameDraft,
            onValueChange = { viewModel.onIntent(TripDetailIntent.TravelerNameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Имя участника") },
            singleLine = true,
        )
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.AddTraveler) },
            enabled = !state.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Добавить участника")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.inviteEmailDraft,
            onValueChange = { viewModel.onIntent(TripDetailIntent.InviteEmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email приглашения") },
            singleLine = true,
        )
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.InviteTraveler) },
            enabled = !state.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Пригласить аккаунт")
        }
        state.lastInvite?.let {
            Text("Invite ${it.status.name.lowercase()}: ${it.email}", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.AddPurchase) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Добавить покупку")
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.OpenDiary) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Дневник")
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.OpenTaxFree) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tax Free")
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.OpenAlerts) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Алерты")
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.OpenMap) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Маршрут")
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.OpenStats) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Статистика")
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.onIntent(TripDetailIntent.OpenExport) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Экспорт")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Покупки (${detail.purchases.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            items(detail.purchases, key = { it.id }) { purchase ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    Text(
                        text = purchase.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "${purchase.amount.toDecimalString()} ${purchase.amount.currency}" +
                            if (purchase.pendingSync) " · sync…" else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
