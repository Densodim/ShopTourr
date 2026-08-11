package com.example.shoptourr.presentation

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.presentation.error.UiErrorAction
import com.example.shoptourr.presentation.error.toUiError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiErrorMappingTest {

    @Test
    fun `unauthorized maps to logout action`() {
        val ui = AppError.Unauthorized.toUiError()
        assertFalse(ui.isRetryable)
        assertEquals(UiErrorAction.Logout, ui.action)
    }

    @Test
    fun `network is retryable`() {
        val ui = AppError.Network.toUiError()
        assertTrue(ui.isRetryable)
    }

    @Test
    fun `validation is not retryable`() {
        val ui = AppError.Validation("email").toUiError()
        assertFalse(ui.isRetryable)
        assertEquals("email", ui.message)
    }
}
