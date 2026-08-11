package com.example.shoptourr.domain.model

import kotlin.math.abs
import kotlin.math.round

/**
 * Money in minor units (cents for 2-decimal currencies).
 * Wire format remains decimal string via [toDecimalString] / [parse].
 */
data class Money(
    val minorUnits: Long,
    val currency: String,
) {
    init {
        require(currency.length == 3) { "currency must be ISO-4217" }
    }

    fun toDecimalString(): String {
        val sign = if (minorUnits < 0) "-" else ""
        val absUnits = abs(minorUnits)
        val whole = absUnits / 100
        val frac = (absUnits % 100).toString().padStart(2, '0')
        return "$sign$whole.$frac"
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "currency mismatch" }
        return Money(minorUnits + other.minorUnits, currency)
    }

    fun splitEqually(parts: Int): List<Money> {
        require(parts > 0) { "parts must be > 0" }
        val base = minorUnits / parts
        val remainder = (minorUnits % parts).toInt()
        return List(parts) { index ->
            Money(base + if (index < remainder) 1 else 0, currency)
        }
    }

    companion object {
        fun zero(currency: String): Money = Money(0, currency)

        fun parse(raw: String, currency: String): Money {
            val cleaned = raw.trim().replace(" ", "").replace(',', '.')
            require(cleaned.isNotEmpty()) { "amount blank" }
            val negative = cleaned.startsWith('-')
            val unsigned = if (negative) cleaned.drop(1) else cleaned
            val parts = unsigned.split('.')
            val whole = parts[0].ifEmpty { "0" }.toLong()
            val frac = (parts.getOrNull(1) ?: "").padEnd(2, '0').take(2).toLong()
            val minor = whole * 100 + frac
            return Money(if (negative) -minor else minor, currency)
        }
    }
}

data class VatBreakdown(
    val net: Money,
    val vat: Money,
    val gross: Money,
    val vatRatePercent: String,
    val vatIncluded: Boolean,
)

object VatCalculator {
    fun breakdown(
        amount: Money,
        vatRatePercent: String,
        vatIncluded: Boolean,
    ): VatBreakdown {
        val rate = vatRatePercent.toDoubleOrNull()
            ?: throw IllegalArgumentException("invalid vat rate")
        require(rate >= 0.0) { "vat rate must be >= 0" }

        return if (vatIncluded) {
            val grossMinor = amount.minorUnits
            val netMinor = if (rate == 0.0) {
                grossMinor
            } else {
                round(grossMinor / (1.0 + rate / 100.0)).toLong()
            }
            val vatMinor = grossMinor - netMinor
            VatBreakdown(
                net = Money(netMinor, amount.currency),
                vat = Money(vatMinor, amount.currency),
                gross = amount,
                vatRatePercent = vatRatePercent,
                vatIncluded = true,
            )
        } else {
            val netMinor = amount.minorUnits
            val vatMinor = round(netMinor * (rate / 100.0)).toLong()
            VatBreakdown(
                net = amount,
                vat = Money(vatMinor, amount.currency),
                gross = Money(netMinor + vatMinor, amount.currency),
                vatRatePercent = vatRatePercent,
                vatIncluded = false,
            )
        }
    }
}
