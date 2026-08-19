package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.FtsQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FtsQueryTest {

    @Test
    fun `blank input is not a match query`() {
        assertNull(FtsQuery.fromUserInput(" "))
        assertNull(FtsQuery.fromUserInput("***"))
    }

    @Test
    fun `tokens become prefix terms and punctuation is stripped`() {
        assertEquals("belem*", FtsQuery.fromUserInput("belem"))
        assertEquals("pasteis* nata*", FtsQuery.fromUserInput("  pasteis, nata "))
        assertEquals("белем*", FtsQuery.fromUserInput("Белем!"))
    }
}
