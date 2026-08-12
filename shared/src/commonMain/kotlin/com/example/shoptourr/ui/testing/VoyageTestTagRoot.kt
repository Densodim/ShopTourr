package com.example.shoptourr.ui.testing

import androidx.compose.runtime.Composable

/** Enables Compose testTags as UiAutomator/Maestro resource ids on Android. */
@Composable
expect fun VoyageTestTagRoot(content: @Composable () -> Unit)
