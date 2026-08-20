package com.example.shoptourr.domain.validation

import com.example.shoptourr.domain.model.SupportedCurrencies
import io.valix.runtime.valixDsl
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Format checks for user-entered fields. Length-only rules let through `@@@` or `eur`;
 * these predicates match the shapes the API actually stores (ISO codes, letters in names,
 * image content types) and stay on `Char` APIs so they work on every KMP target.
 */
object FieldRules {

    fun isPersonName(value: String, min: Int = DISPLAY_NAME_MIN, max: Int = DISPLAY_NAME_MAX): Boolean =
        isHumanName(value, min = min, max = max, allowDigits = false)

    fun isPlaceName(value: String, max: Int = PLACE_NAME_MAX): Boolean =
        isHumanName(value, min = 1, max = max, allowDigits = false)

    fun isTravelerName(value: String): Boolean =
        isHumanName(value, min = 1, max = TRAVELER_NAME_MAX, allowDigits = false)

    fun isItemName(value: String, max: Int = ITEM_NAME_MAX): Boolean =
        isHumanName(value, min = 1, max = max, allowDigits = true, extraPunct = ITEM_PUNCT)

    fun isEmail(value: String): Boolean =
        EmailInputValidator.validate(EmailInput(value)).valid

    fun isIso4217(code: String): Boolean =
        code.length == 3 && code.all { it in 'A'..'Z' }

    fun isSupportedCurrency(code: String): Boolean =
        isIso4217(code) && code in SupportedCurrencies.codes

    fun isCountryCode(code: String): Boolean =
        code.length == 2 && code.all { it in 'A'..'Z' }

    fun isLocale(tag: String): Boolean = tag == "en" || tag == "ru"

    fun isIsoDate(value: String): Boolean =
        value.length == 10 && runCatching { LocalDate.parse(value) }.isSuccess

    fun isIsoTime(value: String): Boolean =
        runCatching { LocalTime.parse(value) }.isSuccess

    fun isHexColor(value: String): Boolean =
        value.length == 7 &&
            value[0] == '#' &&
            value.drop(1).all { it in HEX }

    fun isSha256Hex(value: String): Boolean =
        value.length == 64 && value.all { it in HEX }

    fun isReceiptImageContentType(value: String): Boolean =
        value.lowercase() in RECEIPT_CONTENT_TYPES

    fun isMood(value: String): Boolean {
        if (value.isEmpty() || value.length > MOOD_MAX) return false
        if (value.any { it.isISOControl() || it == '<' || it == '>' }) return false
        return value.any { it.isLetter() || !it.isAscii() }
    }

    fun isFreeText(value: String, max: Int): Boolean {
        if (value.isEmpty() || value.length > max) return false
        if (value.any { it.isISOControl() && it !in ALLOWED_TEXT_CONTROLS }) return false
        return value.any { it.isLetterOrDigit() }
    }

    private fun isHumanName(
        value: String,
        min: Int,
        max: Int,
        allowDigits: Boolean,
        extraPunct: String = NAME_PUNCT,
    ): Boolean {
        if (value.length !in min..max) return false
        if (value.any { it.isISOControl() }) return false
        val allowedPunct = extraPunct.toSet()
        var sawLetterOrDigit = false
        for (char in value) {
            when {
                char.isLetter() || char.isMark() -> {
                    if (char.isLetter()) sawLetterOrDigit = true
                }
                allowDigits && char.isDigit() -> sawLetterOrDigit = true
                char.isWhitespace() -> Unit
                char in allowedPunct -> Unit
                else -> return false
            }
        }
        return sawLetterOrDigit
    }

    private fun Char.isMark(): Boolean = when (category) {
        CharCategory.NON_SPACING_MARK,
        CharCategory.COMBINING_SPACING_MARK,
        CharCategory.ENCLOSING_MARK,
        -> true
        else -> false
    }

    private fun Char.isAscii(): Boolean = code < 128

    private data class EmailInput(val email: String)

    private val EmailInputValidator = valixDsl<EmailInput> {
        field("email", EmailInput::email) {
            notBlank()
            email()
            maxLength(EMAIL_MAX)
        }
    }

    private val HEX = ('0'..'9') + ('a'..'f') + ('A'..'F')
    private val ALLOWED_TEXT_CONTROLS = setOf('\n', '\r', '\t')
    private val RECEIPT_CONTENT_TYPES = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/heic",
        "image/heif",
    )
}

private const val NAME_PUNCT = ".'’,-"
private const val ITEM_PUNCT = ".'’,-()/&+"
internal const val PLACE_NAME_MAX = 120
internal const val ITEM_NAME_MAX = 200
internal const val TRAVELER_NAME_MAX = 60
internal const val MOOD_MAX = 8
internal const val DIARY_TEXT_MAX = 4000
internal const val NOTE_MAX = 500
internal const val PLACE_FIELD_MAX = 200
