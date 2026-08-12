package com.example.shoptourr.appsize

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSizeBudgetTest {

    @Test
    fun `budget is 40 MiB`() {
        assertEquals(40L * 1024L * 1024L, AppSizeBudget.MAX_INSTALL_BYTES)
    }

    @Test
    fun `sizes at or under budget pass`() {
        assertTrue(AppSizeBudget.isWithinBudget(0))
        assertTrue(AppSizeBudget.isWithinBudget(AppSizeBudget.MAX_INSTALL_BYTES))
    }

    @Test
    fun `sizes over budget fail`() {
        assertFalse(AppSizeBudget.isWithinBudget(AppSizeBudget.MAX_INSTALL_BYTES + 1))
    }
}
