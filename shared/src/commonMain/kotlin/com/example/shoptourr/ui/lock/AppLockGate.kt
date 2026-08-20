package com.example.shoptourr.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.shoptourr.presentation.lock.AppLockIntent
import com.example.shoptourr.presentation.lock.AppLockViewModel
import com.example.shoptourr.ui.components.UiErrorBanner
import com.example.shoptourr.ui.components.VoyageButton
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.testing.VoyageTestTags
import com.example.shoptourr.ui.theme.VoyageTokens

@Composable
fun AppLockGate(
    viewModel: AppLockViewModel,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onIntent(AppLockIntent.Bind)
                Lifecycle.Event.ON_STOP -> viewModel.onIntent(AppLockIntent.AppStopped)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (state.locked) {
            AppLockOverlay(
                authenticating = state.authenticating,
                onUnlock = { viewModel.onIntent(AppLockIntent.Unlock) },
            )
            state.error?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    UiErrorBanner(error = err)
                }
            }
        }
    }
}

@Composable
private fun AppLockOverlay(
    authenticating: Boolean,
    onUnlock: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoyageTokens.bg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            )
            .testTag(VoyageTestTags.APP_LOCK_OVERLAY)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = t("app_lock_title"),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = t("app_lock_sub"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(24.dp))
            VoyageButton(
                text = t("app_lock_unlock"),
                onClick = onUnlock,
                isLoading = authenticating,
                modifier = Modifier.testTag(VoyageTestTags.APP_LOCK_UNLOCK),
            )
        }
    }
}
