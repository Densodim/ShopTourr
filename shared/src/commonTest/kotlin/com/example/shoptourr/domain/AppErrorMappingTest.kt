package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.error.asAppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AppErrorMappingTest {

    @Test
    fun `asAppError keeps AppError instances`() {
        assertEquals(AppError.Unauthorized, AppError.Unauthorized.asAppError())
    }

    @Test
    fun `asAppError wraps unknown throwables with origin`() {
        val origin = IllegalStateException("boom")
        val mapped = origin.asAppError()
        assertIs<AppError.Unknown>(mapped)
        assertEquals(origin, mapped.origin)
    }

    @Test
    fun `validation equality uses message`() {
        assertEquals(AppError.Validation("email"), AppError.Validation("email"))
        assertTrue(AppError.Validation("email") != AppError.Validation("password"))
    }
}
