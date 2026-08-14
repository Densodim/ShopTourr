package com.example.shoptourr.i18n

import kotlin.concurrent.Volatile

enum class AppLocale(val tag: String) {
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
}
