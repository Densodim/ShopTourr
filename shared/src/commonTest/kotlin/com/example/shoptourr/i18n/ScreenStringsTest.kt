package com.example.shoptourr.i18n

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Guards the strings the export, tax-free, stats, alerts, diary and wishlist
 * screens rely on. These screens used to carry hardcoded Russian, which left
 * them half-translated for an English user.
 */
class ScreenStringsTest {

    private val screenKeys = listOf(
        // export
        "format", "create_export", "export_preparing", "status", "done",
        "expires", "export_failed", "link_expired",
        // tax free
        "taxfree_calculating", "no_data", "taxfree_empty_sub", "taxfree_estimate",
        "purchases_section",
        // stats
        "calculating", "over_budget",
        // alerts
        "loading", "alerts_empty", "alerts_empty_sub",
        // diary
        "mood", "entry", "diary_empty", "diary_empty_sub",
    )

    @Test
    fun `every screen key resolves in both locales`() {
        screenKeys.forEach { key ->
            listOf(AppLocale.RU, AppLocale.EN).forEach { locale ->
                val value = VoyageI18n.t(locale, key)
                assertTrue(value != key, "key '$key' is missing in $locale")
                assertTrue(value.isNotBlank(), "key '$key' is blank in $locale")
            }
        }
    }

    @Test
    fun `english values are real translations, not the russian fallback`() {
        screenKeys.forEach { key ->
            assertNotEquals(
                VoyageI18n.t(AppLocale.RU, key),
                VoyageI18n.t(AppLocale.EN, key),
                "key '$key' falls back to Russian in English",
            )
        }
    }

    @Test
    fun `no screen key leaks cyrillic into the english catalog`() {
        val cyrillic = Regex("[А-Яа-яЁё]")
        VoyageCatalog.en.forEach { (key, value) ->
            assertTrue(
                !cyrillic.containsMatchIn(value),
                "english value for '$key' contains cyrillic: $value",
            )
        }
    }
}
