package com.example.shoptourr.domain

import com.example.shoptourr.domain.validation.FieldRules
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FieldRulesTest {

    @Test
    fun `person names accept letters including accents and apostrophes`() {
        listOf("Mila", "Ada Lovelace", "O'Brien", "Côte", "Мария", "São Paulo".substringBefore(' '))
            .forEach { assertTrue(FieldRules.isPersonName(it), it) }
        assertTrue(FieldRules.isPersonName("Jean-Luc"))
        assertTrue(FieldRules.isPersonName("O’Brien"))
    }

    @Test
    fun `person names reject symbols digits and control characters`() {
        listOf("@@@", "<script>", "Ada!!!", "User123", "A", "M".repeat(81), "  ")
            .forEach { assertFalse(FieldRules.isPersonName(it.trim().ifEmpty { it }), it) }
        assertFalse(FieldRules.isPersonName("Ada\u0007"))
    }

    @Test
    fun `place names accept cities and reject numeric or punctuation soup`() {
        assertTrue(FieldRules.isPlaceName("Lisbon"))
        assertTrue(FieldRules.isPlaceName("St. Petersburg"))
        assertTrue(FieldRules.isPlaceName("Côte d'Ivoire"))
        assertTrue(FieldRules.isPlaceName("New York"))
        assertFalse(FieldRules.isPlaceName("@@@"))
        assertFalse(FieldRules.isPlaceName("123"))
        assertFalse(FieldRules.isPlaceName("Lisbon!!!"))
    }

    @Test
    fun `item names allow digits and shop punctuation but not tags`() {
        assertTrue(FieldRules.isItemName("iPhone 16"))
        assertTrue(FieldRules.isItemName("T-shirt (M)"))
        assertTrue(FieldRules.isItemName("H&M"))
        assertFalse(FieldRules.isItemName("@@@"))
        assertFalse(FieldRules.isItemName("<script>"))
        assertFalse(FieldRules.isItemName("!!!"))
    }

    @Test
    fun `email matches the valix contract not a lone at-sign`() {
        assertTrue(FieldRules.isEmail("mila@voyage.app"))
        assertFalse(FieldRules.isEmail("not-an-email"))
        assertFalse(FieldRules.isEmail("a@b"))
        assertFalse(FieldRules.isEmail("friend@"))
    }

    @Test
    fun `currency and country codes are uppercase ISO`() {
        assertTrue(FieldRules.isIso4217("EUR"))
        assertTrue(FieldRules.isSupportedCurrency("EUR"))
        assertFalse(FieldRules.isIso4217("eur"))
        assertFalse(FieldRules.isIso4217("EU"))
        assertFalse(FieldRules.isSupportedCurrency("XXX"))
        assertTrue(FieldRules.isCountryCode("PT"))
        assertFalse(FieldRules.isCountryCode("pt"))
        assertFalse(FieldRules.isCountryCode("P"))
        assertFalse(FieldRules.isCountryCode("P1"))
    }

    @Test
    fun `locale is only the app languages`() {
        assertTrue(FieldRules.isLocale("en"))
        assertTrue(FieldRules.isLocale("ru"))
        assertFalse(FieldRules.isLocale("en-US"))
        assertFalse(FieldRules.isLocale("de"))
        assertFalse(FieldRules.isLocale("xx"))
    }

    @Test
    fun `dates and times must be ISO calendar values`() {
        assertTrue(FieldRules.isIsoDate("2026-04-12"))
        assertFalse(FieldRules.isIsoDate("12.04.2026"))
        assertFalse(FieldRules.isIsoDate("not-a-date"))
        assertFalse(FieldRules.isIsoDate("2026-13-40"))
        assertTrue(FieldRules.isIsoTime("10:24"))
        assertTrue(FieldRules.isIsoTime("10:24:00"))
        assertFalse(FieldRules.isIsoTime("25:00"))
        assertFalse(FieldRules.isIsoTime("noon"))
    }

    @Test
    fun `hex color receipt type and mood reject garbage`() {
        assertTrue(FieldRules.isHexColor("#FFD84D"))
        assertFalse(FieldRules.isHexColor("FFD84D"))
        assertFalse(FieldRules.isHexColor("#GGGGGG"))
        assertTrue(FieldRules.isReceiptImageContentType("image/jpeg"))
        assertFalse(FieldRules.isReceiptImageContentType("text/html"))
        assertTrue(FieldRules.isMood("good"))
        assertTrue(FieldRules.isMood("😊"))
        assertFalse(FieldRules.isMood("!!!!!!!!"))
        assertFalse(FieldRules.isMood("<svg>"))
    }

    @Test
    fun `free text needs a letter or digit and no binary controls`() {
        assertTrue(FieldRules.isFreeText("Pasteis in Belem", max = 4000))
        assertTrue(FieldRules.isFreeText("Day 1\nWalked", max = 4000))
        assertFalse(FieldRules.isFreeText("!!!", max = 4000))
        assertFalse(FieldRules.isFreeText("hi\u0000there", max = 4000))
    }
}
