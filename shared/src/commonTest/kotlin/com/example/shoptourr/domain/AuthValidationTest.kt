package com.example.shoptourr.domain

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.validation.ForgotPasswordForm
import com.example.shoptourr.domain.validation.ForgotPasswordFormValidator
import com.example.shoptourr.domain.validation.LoginForm
import com.example.shoptourr.domain.validation.LoginFormValidator
import com.example.shoptourr.domain.validation.RegisterForm
import com.example.shoptourr.domain.validation.RegisterFormValidator
import com.example.shoptourr.domain.validation.ResetPasswordForm
import com.example.shoptourr.domain.validation.ResetPasswordFormValidator
import com.example.shoptourr.domain.validation.toValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The Valix DSL builds validators at runtime, so a typo in a rule is not a compile error the way
 * a missing generated validator would be. These tests are what keeps the form rules honest.
 */
class AuthValidationTest {

    private fun login(email: String = "mila@voyage.app", password: String = "secret1") =
        LoginFormValidator.validate(LoginForm(email, password)).toValidationError()?.message

    private fun register(
        displayName: String = "Mila",
        email: String = "mila@voyage.app",
        password: String = "secret12",
        locale: String = "ru",
    ) = RegisterFormValidator.validate(RegisterForm(displayName, email, password, locale))
        .toValidationError()?.message

    @Test
    fun `a well formed login passes`() {
        assertNull(login())
    }

    @Test
    fun `login rejects a blank email`() {
        assertEquals("email", login(email = " "))
    }

    @Test
    fun `login rejects a password below six characters`() {
        assertEquals("password", login(password = "123"))
    }

    @Test
    fun `login accepts a six character password`() {
        assertNull(login(password = "abcdef"))
    }

    @Test
    fun `login rejects a malformed email that the old contains-at check let through`() {
        assertEquals("email", login(email = "not-an-email"))
    }

    @Test
    fun `a well formed registration passes`() {
        assertNull(register())
    }

    @Test
    fun `registration keeps the stricter eight character password floor`() {
        assertEquals("password", register(password = "secret1"))
        assertNull(register(password = "secret12"))
    }

    @Test
    fun `registration rejects a display name shorter than the server minimum`() {
        assertEquals("displayName", register(displayName = "M"))
    }

    @Test
    fun `registration rejects a display name past the server maximum`() {
        assertEquals("displayName", register(displayName = "M".repeat(81)))
        assertNull(register(displayName = "M".repeat(80)))
    }

    @Test
    fun `registration rejects an email past the server maximum`() {
        val longEmail = "a".repeat(250) + "@voyage.app"
        assertEquals("email", register(email = longEmail))
    }

    @Test
    fun `forgot password rejects a malformed email`() {
        val error = ForgotPasswordFormValidator.validate(ForgotPasswordForm("nope"))
            .toValidationError()
        assertEquals(AppError.Validation("email"), error)
    }

    @Test
    fun `reset password enforces the token bounds from the server contract`() {
        fun reset(token: String) = ResetPasswordFormValidator
            .validate(ResetPasswordForm("mila@voyage.app", token, "secret1"))
            .toValidationError()?.message

        assertEquals("token", reset("short"))
        assertEquals("token", reset("t".repeat(129)))
        assertNull(reset("t".repeat(16)))
    }

    @Test
    fun `the first failing field is the one reported`() {
        val error = RegisterFormValidator
            .validate(RegisterForm(displayName = "", email = "bad", password = "x", locale = "ru"))
            .toValidationError()

        assertEquals(AppError.Validation("displayName"), error)
    }

    @Test
    fun `a valid form yields no error at all`() {
        val result = LoginFormValidator.validate(LoginForm("mila@voyage.app", "secret1"))

        assertTrue(result.valid)
        assertNull(result.toValidationError())
    }
}
