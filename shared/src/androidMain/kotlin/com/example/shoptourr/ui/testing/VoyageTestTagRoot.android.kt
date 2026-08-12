package com.example.shoptourr.ui.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun VoyageTestTagRoot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.semantics {
            testTagsAsResourceId = true
        },
    ) {
        content()
    }
}
