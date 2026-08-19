package com.example.shoptourr.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VoyageI18nTest {

    @Test
    fun `resolves russian and english keys`() {
        assertEquals("Карта", VoyageI18n.t(AppLocale.RU, "map"))
        assertEquals("Map", VoyageI18n.t(AppLocale.EN, "map"))
    }

    @Test
    fun `falls back to russian then key`() {
        assertEquals("missing_key", VoyageI18n.t(AppLocale.EN, "missing_key"))
        assertEquals("missing_key", VoyageI18n.t(AppLocale.RU, "missing_key"))
    }

    @Test
    fun `interpolates day placeholders`() {
        val text = VoyageI18n.t(
            AppLocale.RU,
            "day_n_of",
            mapOf("n" to "4", "m" to "7"),
        )
        assertEquals("день 4 из 7", text)
    }

    @Test
    fun `parses locale tags`() {
        assertEquals(AppLocale.RU, AppLocale.fromTag("ru"))
        assertEquals(AppLocale.EN, AppLocale.fromTag("en-US"))
        assertEquals(AppLocale.RU, AppLocale.fromTag(null))
        assertEquals(AppLocale.RU, AppLocale.fromTag("xx"))
    }

    @Test
    fun `ru and en stay left to right`() {
        assertEquals(false, AppLocale.RU.isRtl)
        assertEquals(false, AppLocale.EN.isRtl)
    }

    @Test
    fun `welcome title has no html tags`() {
        val title = VoyageI18n.t(AppLocale.RU, "welcome_title")
        assertEquals(false, title.contains("<"))
        assertEquals(true, title.contains("глава истории"))
    }

    @Test
    fun `resolves the open affordance used by the current trip card`() {
        assertEquals("Открыть", VoyageI18n.t(AppLocale.RU, "open"))
        assertEquals("Open", VoyageI18n.t(AppLocale.EN, "open"))
    }

    @Test
    fun `russian and english catalogs cover the same keys`() {
        assertEquals(VoyageCatalog.ru.keys, VoyageCatalog.en.keys)
    }

    @Test
    fun `reset password screen is really translated and not falling back to russian`() {
        listOf("reset_password", "reset_password_sub", "reset_token", "reset_done", "reset_done_sub")
            .forEach { key ->
                assertNotEquals(
                    VoyageI18n.t(AppLocale.RU, key),
                    VoyageI18n.t(AppLocale.EN, key),
                    "key '$key' resolves to the same text in both locales",
                )
            }
    }
}
