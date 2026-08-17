package com.example.shoptourr.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class VoyageTokensTest {

    @Test
    fun `matches mila css palette`() {
        assertEquals(Color(0xFFF1EDE4), VoyageTokens.bg) // --paper
        assertEquals(Color(0xFFFBF9F4), VoyageTokens.surface) // --card
        assertEquals(Color(0xFFF5F1E8), VoyageTokens.surface2) // --card-2
        assertEquals(Color(0xFFE9E4D8), VoyageTokens.surface3) // --paper-2
        assertEquals(Color(0xFF9C3B28), VoyageTokens.accent)
        assertEquals(Color(0xFF7E2E1E), VoyageTokens.accent2)
        assertEquals(Color(0xFF1C1917), VoyageTokens.ink)
        assertEquals(Color(0xFF3E3831), VoyageTokens.ink2)
        assertEquals(Color(0xFF8A8177), VoyageTokens.muted)
        assertEquals(Color(0xFFB3AA9C), VoyageTokens.muted2)
        assertEquals(Color(0xFFDDD6C7), VoyageTokens.border) // --rule
        assertEquals(Color(0xFFE8E2D5), VoyageTokens.border2) // --rule-2
        assertEquals(Color(0xFF9C3B28), VoyageTokens.danger)
        assertEquals(Color(0xFF4C5D4A), VoyageTokens.success) // --sage
    }
}
