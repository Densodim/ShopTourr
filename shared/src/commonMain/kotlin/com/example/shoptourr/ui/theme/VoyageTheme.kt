package com.example.shoptourr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VoyageLightColors = lightColorScheme(
    // The design's primary action (.primary-btn) is ink on paper; oxblood is the
    // emphasis colour for links, active states and hover, so it maps to secondary.
    primary = VoyageTokens.ink,
    onPrimary = VoyageTokens.bg,
    secondary = VoyageTokens.accent,
    onSecondary = VoyageTokens.bg,
    background = VoyageTokens.bg,
    onBackground = VoyageTokens.ink,
    surface = VoyageTokens.surface,
    onSurface = VoyageTokens.ink,
    surfaceVariant = VoyageTokens.surface2,
    onSurfaceVariant = VoyageTokens.muted,
    error = VoyageTokens.danger,
    onError = VoyageTokens.bg,
    outline = VoyageTokens.border,
    outlineVariant = VoyageTokens.border2,
)

/**
 * Editorial split: serif for headings and amounts, monospace for eyebrows and
 * small labels, sans for body copy and buttons. Instrument Serif and JetBrains
 * Mono are not bundled, so the platform serif / mono families stand in.
 */
private val VoyageTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.84).sp,
        color = VoyageTokens.ink,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.56).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.22).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // .primary-btn — 13px / 500 / .08em, uppercased at the call site.
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.04.sp,
    ),
    // .nav-title / .eyebrow — mono, wide tracking, uppercase.
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.68.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.6.sp,
    ),
)

// Paper & ink squares everything off; only the interactive controls that map to
// `medium` (.primary-btn, .ghost-btn, .social-btn) keep the 2px the design gives them.
private val VoyageShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun VoyageTheme(content: @Composable () -> Unit) {
    // The design is light-only; there is no dark counterpart to switch to.
    MaterialTheme(
        colorScheme = VoyageLightColors,
        typography = VoyageTypography,
        shapes = VoyageShapes,
        content = content,
    )
}

val VoyageColors
    @Composable get() = MaterialTheme.colorScheme
