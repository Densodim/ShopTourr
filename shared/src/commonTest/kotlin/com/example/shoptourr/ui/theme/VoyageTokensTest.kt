package com.example.shoptourr.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class VoyageTokensTest {

    @Test
    fun `matches mila css palette`() {
        assertEquals(Color(0xFF0A0A0A), VoyageTokens.bg)
        assertEquals(Color(0xFF141414), VoyageTokens.surface)
        assertEquals(Color(0xFFFFD84D), VoyageTokens.accent)
        assertEquals(Color(0xFFFAFAFA), VoyageTokens.ink)
        assertEquals(Color(0xFF2A2A2A), VoyageTokens.border)
        assertEquals(Color(0xFFF87171), VoyageTokens.danger)
        assertEquals(Color(0xFF4ADE80), VoyageTokens.success)
    }
}
