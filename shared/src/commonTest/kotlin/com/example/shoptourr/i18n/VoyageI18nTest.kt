package com.example.shoptourr.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `welcome title has no html tags`() {
        val title = VoyageI18n.t(AppLocale.RU, "welcome_title")
        assertEquals(false, title.contains("<"))
        assertEquals(true, title.contains("глава истории"))
    }
}
