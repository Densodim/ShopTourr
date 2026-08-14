package com.example.shoptourr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.shoptourr.presentation.error.UiError
import com.example.shoptourr.ui.i18n.t
import com.example.shoptourr.ui.theme.VoyageTokens

enum class VoyageButtonVariant { Primary, Secondary, Ghost }

@Composable
fun VoyageButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: VoyageButtonVariant = VoyageButtonVariant.Primary,
    fillMaxWidth: Boolean = true,
) {
    val shape = MaterialTheme.shapes.medium
    val widthModifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier
    when (variant) {
        VoyageButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.then(widthModifier).height(52.dp).semantics { contentDescription = text },
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                VoyageButtonLabel(text = text, isLoading = isLoading, onPrimary = true)
            }
        }
        VoyageButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.then(widthModifier).height(52.dp).semantics { contentDescription = text },
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                VoyageButtonLabel(text = text, isLoading = isLoading, onPrimary = false)
            }
        }
        VoyageButtonVariant.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.then(widthModifier).height(48.dp).semantics { contentDescription = text },
                enabled = enabled && !isLoading,
            ) {
                VoyageButtonLabel(text = text, isLoading = isLoading, onPrimary = false)
            }
        }
    }
}

@Composable
private fun VoyageButtonLabel(text: String, isLoading: Boolean, onPrimary: Boolean) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = if (onPrimary) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primary
            },
            strokeWidth = 2.dp,
        )
    } else {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun VoyageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    errorMessage: String? = null,
    testTag: String? = null,
) {
    val tagged = if (testTag != null) {
        modifier
            .fillMaxWidth()
            .testTag(testTag)
            .semantics { contentDescription = label }
    } else {
        modifier
            .fillMaxWidth()
            .semantics { contentDescription = label }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = tagged,
        label = { Text(label) },
        singleLine = singleLine,
        enabled = enabled,
        isError = errorMessage != null,
        supportingText = errorMessage?.let { message ->
            { Text(message, color = MaterialTheme.colorScheme.error) }
        },
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorCursorColor = MaterialTheme.colorScheme.error,
        ),
    )
}

@Composable
fun VoyageTopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("← ${t("back")}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(4.dp))
            }
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        actions()
    }
}

@Composable
fun VoyageEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun VoyageSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        VoyageEyebrow(title)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun VoyageSurfaceBlock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large)
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun UiErrorBanner(
    error: UiError,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
            .padding(14.dp),
    ) {
        Text(t(error.titleKey), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            error.messageOverride ?: t(error.messageKey),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (onRetry != null && error.isRetryable) {
            Spacer(Modifier.height(8.dp))
            VoyageButton(
                text = t("retry"),
                onClick = onRetry,
                variant = VoyageButtonVariant.Secondary,
            )
        }
    }
}

@Composable
fun LoadingBlock(modifier: Modifier = Modifier, label: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (label != null) {
            Spacer(Modifier.height(12.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(14.dp))
            VoyageButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun VoyageScreen(
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoyageTokens.bg)
            .background(
                Brush.verticalGradient(
                    colors = listOf(VoyageTokens.glow, VoyageTokens.bg),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .then(if (scrollable) Modifier.verticalScroll(scroll) else Modifier)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            content = content,
        )
    }
}

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoyageTokens.bg)
            .background(
                Brush.verticalGradient(
                    colors = listOf(VoyageTokens.glow, VoyageTokens.bg),
                ),
            )
            .safeContentPadding(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
