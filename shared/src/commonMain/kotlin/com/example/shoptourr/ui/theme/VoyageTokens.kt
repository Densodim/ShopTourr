package com.example.shoptourr.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens mirrored from `.mila-design/styles.css` (Voyage — Modern Classic,
 * "paper & ink"). Names are kept from the previous dark-premium palette so call
 * sites stay untouched; only the values moved from ink-on-black to ink-on-paper.
 */
object VoyageTokens {
    val bg = Color(0xFFF1EDE4) // --paper
    val surface = Color(0xFFFBF9F4) // --card
    val surface2 = Color(0xFFF5F1E8) // --card-2
    val surface3 = Color(0xFFE9E4D8) // --paper-2
    val border = Color(0xFFDDD6C7) // --rule
    val border2 = Color(0xFFE8E2D5) // --rule-2
    val ink = Color(0xFF1C1917)
    val ink2 = Color(0xFF3E3831)
    val muted = Color(0xFF8A8177)
    val muted2 = Color(0xFFB3AA9C)
    val accent = Color(0xFF9C3B28) // oxblood
    val accent2 = Color(0xFF7E2E1E)
    val accentDim = Color(0x179C3B28) // rgba(156,59,40,0.09)
    val success = Color(0xFF4C5D4A) // --sage
    val danger = Color(0xFF9C3B28)
    val glow = Color(0x0F9C3B28) // soft ambient wash (~6% accent)
}
