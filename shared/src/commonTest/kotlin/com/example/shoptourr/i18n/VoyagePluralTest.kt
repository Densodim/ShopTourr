package com.example.shoptourr.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

class VoyagePluralTest {

    @Test
    fun `russian picks the singular paucal and plural forms`() {
        assertEquals("1 покупка", VoyageI18n.plural(AppLocale.RU, "purchases", 1))
        assertEquals("3 покупки", VoyageI18n.plural(AppLocale.RU, "purchases", 3))
        assertEquals("7 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 7))
    }

    @Test
    fun `russian teens take the plural form rather than the singular`() {
        assertEquals("11 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 11))
        assertEquals("12 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 12))
        assertEquals("14 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 14))
    }

    @Test
    fun `russian repeats the pattern past twenty`() {
        assertEquals("21 покупка", VoyageI18n.plural(AppLocale.RU, "purchases", 21))
        assertEquals("22 покупки", VoyageI18n.plural(AppLocale.RU, "purchases", 22))
        assertEquals("25 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 25))
        assertEquals("111 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 111))
    }

    @Test
    fun `zero takes the plural form`() {
        assertEquals("0 покупок", VoyageI18n.plural(AppLocale.RU, "purchases", 0))
    }

    @Test
    fun `english only distinguishes one from the rest`() {
        assertEquals("1 purchase", VoyageI18n.plural(AppLocale.EN, "purchases", 1))
        assertEquals("2 purchases", VoyageI18n.plural(AppLocale.EN, "purchases", 2))
        assertEquals("0 purchases", VoyageI18n.plural(AppLocale.EN, "purchases", 0))
        assertEquals("11 purchases", VoyageI18n.plural(AppLocale.EN, "purchases", 11))
    }
}
