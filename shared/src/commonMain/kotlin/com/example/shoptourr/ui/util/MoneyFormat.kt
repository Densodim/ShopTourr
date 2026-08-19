package com.example.shoptourr.ui.util

import com.example.shoptourr.domain.model.Money

/**
 * Amounts as a reader scans them: symbol first, thousands split by a thin space.
 * `Money.toDecimalString()` plus a trailing ISO code reads as data ("1584.50 EUR")
 * and, at four figures, is wide enough to wrap the line it sits on.
 */
fun Money.formatted(): String {
    val symbol = currencySymbol(currency)
    val negative = minorUnits < 0
    val abs = if (negative) -minorUnits else minorUnits
    val whole = groupThousands(abs / 100)
    val frac = (abs % 100).toString().padStart(2, '0')
    val sign = if (negative) "-" else ""
    return if (symbol != null) "$sign$symbol$whole.$frac" else "$sign$whole.$frac $currency"
}

/** Whole units only — for pace figures where cents are noise. */
fun Money.formattedRounded(): String {
    val symbol = currencySymbol(currency)
    val negative = minorUnits < 0
    val abs = if (negative) -minorUnits else minorUnits
    val whole = groupThousands((abs + 50) / 100)
    val sign = if (negative) "-" else ""
    return if (symbol != null) "$sign$symbol$whole" else "$sign$whole $currency"
}

private fun groupThousands(value: Long): String {
    val digits = value.toString()
    if (digits.length <= 3) return digits
    return digits
        .reversed()
        .chunked(3)
        // Narrow NO-BREAK space: a plain thin space lets "€2 400.00" wrap between
        // the thousands and the rest, splitting one number across two lines.
        .joinToString("\u202F")
        .reversed()
}

/** Only currencies with an unambiguous single-glyph symbol; the rest keep the code. */
private fun currencySymbol(code: String): String? = when (code) {
    "EUR" -> "€"
    "USD" -> "$"
    "GBP" -> "£"
    "JPY" -> "¥"
    "RUB" -> "₽"
    "TRY" -> "₺"
    "KRW" -> "₩"
    "INR" -> "₹"
    "PLN" -> "zł "
    "CZK" -> "Kč "
    else -> null
}

/**
 * An FX rate at reading precision. The API sends full scale — `98.39826700` —
 * which is eight digits of noise next to a two-decimal amount.
 */
fun formatFxRate(raw: String): String {
    val value = raw.toDoubleOrNull() ?: return raw
    // Small rates need their leading zeros to say anything at all.
    val decimals = if (value != 0.0 && value < 1.0) 6 else 4
    val trimmed = raw.substringBefore('.').let { whole ->
        val frac = raw.substringAfter('.', "").take(decimals).trimEnd('0')
        if (frac.isEmpty()) whole else "$whole.$frac"
    }
    return trimmed
}
