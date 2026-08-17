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
import org.jetbrains.compose.resources.Font
import shoptourr.shared.generated.resources.Res
import shoptourr.shared.generated.resources.jetbrains_mono_regular

private val VoyageLightColors = lightColorScheme(
    // Oxblood is the emphasis colour — links, active chips, highlighted amounts —
    // and the app already reaches for `primary` at those call sites. The one place
    // the design wants ink instead is `.primary-btn`, which sets it on its own.
    primary = VoyageTokens.accent,
    onPrimary = VoyageTokens.bg,
    secondary = VoyageTokens.ink,
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
 * small labels, sans for body copy and buttons.
 *
 * JetBrains Mono is bundled (see `composeResources/font`) because it covers
 * Cyrillic and this app is Russian-first. Instrument Serif is deliberately not:
 * it ships Latin only, so Russian headings would fall back and a mixed line
 * ("Привет, Dima") would render in two faces. The mock hits the same wall — its
 * stack is `'Instrument Serif', Georgia, serif` — so Russian headings there are
 * Georgia, and the platform serif is the honest match.
 */
@Composable
private fun voyageTypography(): Typography {
    val serif = FontFamily.Serif
    val mono = FontFamily(Font(Res.font.jetbrains_mono_regular, FontWeight.Normal))
    return Typography(
        displayLarge = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 42.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.84).sp,
            color = VoyageTokens.ink,
        ),
        headlineMedium = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.56).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.22).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = serif,
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
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.68.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            letterSpacing = 1.6.sp,
        ),
    )
}

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
        typography = voyageTypography(),
        shapes = VoyageShapes,
        content = content,
    )
}

val VoyageColors
    @Composable get() = MaterialTheme.colorScheme
