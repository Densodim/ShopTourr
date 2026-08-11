package com.example.shoptourr.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.map.RouteIntent
import com.example.shoptourr.presentation.map.RouteUiEvent
import com.example.shoptourr.presentation.map.RouteViewModel

@Composable
fun RouteScreen(
    viewModel: RouteViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RouteUiEvent.NavigateBack -> onBack()
                RouteUiEvent.Logout -> onLoggedOut()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
    ) {
        TextButton(onClick = { viewModel.onIntent(RouteIntent.Back) }) {
            Text("Назад")
        }
        Text(
            text = "Маршрут",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.onIntent(RouteIntent.Refresh) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Обновить")
        }
        state.error?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err.title, color = MaterialTheme.colorScheme.error)
            Text(err.message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading && state.route == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            return
        }
        val route = state.route
        if (route == null) {
            Text("Нет маршрута", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }
        Text(
            text = "Точек: ${route.stopCount}",
            color = MaterialTheme.colorScheme.onBackground,
        )
        route.distanceMeters?.let {
            Text(text = "Дистанция: ${it}m", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(route.stops.sortedBy { it.orderIndex }, key = { it.id }) { stop ->
                Text(
                    text = "${stop.orderIndex + 1}. ${stop.title}",
                    color = MaterialTheme.colorScheme.onBackground,
                )
                stop.place?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                stop.point?.let {
                    Text("${it.lat}, ${it.lng}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                stop.amountSpentHere?.let {
                    Text(
                        "${it.toDecimalString()} ${it.currency}",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
