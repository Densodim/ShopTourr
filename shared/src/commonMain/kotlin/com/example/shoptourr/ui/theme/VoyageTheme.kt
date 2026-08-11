package com.example.shoptourr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VoyageDarkColors = darkColorScheme(
    primary = Color(0xFFFFD84D),
    onPrimary = Color(0xFF0A0A0A),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF161616),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF222222),
    onSurfaceVariant = Color(0xFFA3A3A3),
    error = Color(0xFFF59890),
)

@Composable
fun VoyageTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoyageDarkColors,
        content = content,
    )
}
