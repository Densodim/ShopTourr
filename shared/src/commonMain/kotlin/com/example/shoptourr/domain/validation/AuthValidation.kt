package com.example.shoptourr.domain.validation

import com.example.shoptourr.domain.error.AppError
import io.valix.core.ValidationResult
import io.valix.runtime.PropertyValidationBuilder
import io.valix.runtime.valixDsl

/**
 * Auth form rules, declared once and mirroring the server contract in
 * `com.shoptourr.identity.dto` so a payload the API would reject never leaves the device.
 *
 * Valix is wired through its runtime DSL rather than the KSP annotation processor: KSP has no
 * release for Kotlin 2.4.x, which this module targets, so the generated-validator route the
 * backend uses is unavailable here. The DSL is ordinary common code and works on every target.
 *
 * Fields are declared in the order the screens present them, and the use cases surface only the
 * first failure, so the reported field stays the one it was before Valix.
 */

/** The DSL has `minLength` but no upper bound, so length ceilings go through `rule`. */
private fun PropertyValidationBuilder<String>.maxLength(limit: Int) =
    rule("MAX_LENGTH", "maximum length is $limit") { it.length <= limit }

data class LoginForm(val email: String, val password: String)

data class RegisterForm(
    val displayName: String,
    val email: String,
    val password: String,
    val locale: String,
)

data class ForgotPasswordForm(val email: String)

data class ResetPasswordForm(val email: String, val token: String, val newPassword: String)

/** Server: `LoginRequest` — email `@NotBlank @Email`, password `@NotBlank @MaxLength(128)`. */
val LoginFormValidator = valixDsl<LoginForm> {
    field("email", LoginForm::email) {
        notBlank()
        email()
        maxLength(EMAIL_MAX)
    }
    field("password", LoginForm::password) {
        notBlank()
        minLength(PASSWORD_MIN_LOGIN)
        maxLength(PASSWORD_MAX)
    }
}

/**
 * Server: `RegisterRequest` — displayName 2..80, email `@Email` max 254, password 6..128,
 * locale 2..5.
 *
 * [PASSWORD_MIN_REGISTER] stays at the app's own stricter 8 rather than the server's 6: that is
 * existing product behaviour and tightening a password floor on sign-up is a deliberate choice,
 * not drift to be silently normalised away.
 */
val RegisterFormValidator = valixDsl<RegisterForm> {
    field("displayName", RegisterForm::displayName) {
        notBlank()
        minLength(DISPLAY_NAME_MIN)
        maxLength(DISPLAY_NAME_MAX)
    }
    field("email", RegisterForm::email) {
        notBlank()
        email()
        maxLength(EMAIL_MAX)
    }
    field("password", RegisterForm::password) {
        notBlank()
        minLength(PASSWORD_MIN_REGISTER)
        maxLength(PASSWORD_MAX)
    }
    field("locale", RegisterForm::locale) {
        minLength(LOCALE_MIN)
        maxLength(LOCALE_MAX)
    }
}

/** Server: `ForgotPasswordRequest` — email `@NotBlank @Email`. */
val ForgotPasswordFormValidator = valixDsl<ForgotPasswordForm> {
    field("email", ForgotPasswordForm::email) {
        notBlank()
        email()
        maxLength(EMAIL_MAX)
    }
}

/** Server: `ResetPasswordRequest` — email `@Email`, token 16..128, newPassword 6..128. */
val ResetPasswordFormValidator = valixDsl<ResetPasswordForm> {
    field("email", ResetPasswordForm::email) {
        notBlank()
        email()
        maxLength(EMAIL_MAX)
    }
    field("token", ResetPasswordForm::token) {
        notBlank()
        minLength(TOKEN_MIN)
        maxLength(TOKEN_MAX)
    }
    field("newPassword", ResetPasswordForm::newPassword) {
        notBlank()
        minLength(PASSWORD_MIN_RESET)
        maxLength(PASSWORD_MAX)
    }
}

/**
 * Collapses a Valix result into the [AppError.Validation] the presentation layer already
 * understands, keyed by the first offending field. Returns `null` when the form is valid.
 */
fun ValidationResult.toValidationError(): AppError.Validation? =
    if (valid) null else AppError.Validation(errors.first().field)

internal const val DISPLAY_NAME_MIN = 2
internal const val DISPLAY_NAME_MAX = 80
internal const val EMAIL_MAX = 254
internal const val PASSWORD_MIN_LOGIN = 6
internal const val PASSWORD_MIN_REGISTER = 8
internal const val PASSWORD_MIN_RESET = 6
internal const val PASSWORD_MAX = 128
internal const val TOKEN_MIN = 16
internal const val TOKEN_MAX = 128
internal const val LOCALE_MIN = 2
internal const val LOCALE_MAX = 5
