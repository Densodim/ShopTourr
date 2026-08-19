package com.example.shoptourr.i18n

import kotlin.concurrent.Volatile

enum class AppLocale(val tag: String, val isRtl: Boolean = false) {
    RU("ru"),
    EN("en"),
    ;

    companion object {
        fun fromTag(raw: String?): AppLocale {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.tag == normalized }
                ?: entries.firstOrNull { normalized.startsWith(it.tag) }
                ?: RU
        }
    }
}

object VoyageI18n {
    @Volatile
    var currentLocale: AppLocale = AppLocale.RU

    fun t(
        locale: AppLocale,
        key: String,
        vars: Map<String, String> = emptyMap(),
    ): String {
        val catalog = when (locale) {
            AppLocale.RU -> VoyageCatalog.ru
            AppLocale.EN -> VoyageCatalog.en
        }
        var value = catalog[key] ?: VoyageCatalog.ru[key] ?: key
        vars.forEach { (name, replacement) ->
            value = value.replace("{$name}", replacement)
        }
        return value
    }

    /**
     * Counted noun for [base] — "1 покупка" / "3 покупки" / "7 покупок". Russian
     * needs three forms, which a single catalog string cannot carry, so this looks
     * up `<base>_one|few|many` and fills `{n}`.
     */
    fun plural(locale: AppLocale, base: String, count: Int): String {
        val suffix = when (locale) {
            AppLocale.RU -> russianForm(count)
            AppLocale.EN -> if (count == 1) "one" else "many"
        }
        return t(locale, "${base}_$suffix", mapOf("n" to count.toString()))
    }

    private fun russianForm(count: Int): String {
        val n = if (count < 0) -count else count
        // 11..14 look like 1..4 but take the plural form.
        if (n % 100 in 11..14) return "many"
        return when (n % 10) {
            1 -> "one"
            2, 3, 4 -> "few"
            else -> "many"
        }
    }
}
