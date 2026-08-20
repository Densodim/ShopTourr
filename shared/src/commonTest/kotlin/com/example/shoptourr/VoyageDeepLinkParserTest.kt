package com.example.shoptourr

import com.example.shoptourr.navigation.VoyageDeepLink
import com.example.shoptourr.navigation.VoyageDeepLinkParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class VoyageDeepLinkParserTest {

    @Test
    fun `parses trip alerts deep link`() {
        val link = VoyageDeepLinkParser.parse("voyage://trips/lisbon/alerts")
        assertIs<VoyageDeepLink.TripAlerts>(link)
        assertEquals("lisbon", link.tripId)
    }

    @Test
    fun `parses https tax-free path`() {
        val link = VoyageDeepLinkParser.parse("https://voyage.app/trips/tokyo/tax-free")
        assertIs<VoyageDeepLink.TripTaxFree>(link)
        assertEquals("tokyo", link.tripId)
    }

    @Test
    fun `parses home`() {
        assertEquals(VoyageDeepLink.Home, VoyageDeepLinkParser.parse("voyage://home"))
    }

    @Test
    fun `parses add-purchase shortcut without a trip`() {
        val link = VoyageDeepLinkParser.parse("voyage://purchases/new")
        assertIs<VoyageDeepLink.AddPurchase>(link)
        assertNull(link.tripId)
    }

    @Test
    fun `parses add-purchase alias`() {
        val link = VoyageDeepLinkParser.parse("voyage://add-purchase")
        assertIs<VoyageDeepLink.AddPurchase>(link)
        assertNull(link.tripId)
    }

    @Test
    fun `parses add purchase on a known trip`() {
        val link = VoyageDeepLinkParser.parse("voyage://trips/lisbon/purchases")
        assertIs<VoyageDeepLink.AddPurchase>(link)
        assertEquals("lisbon", link.tripId)
    }

    @Test
    fun `rejects blank`() {
        assertNull(VoyageDeepLinkParser.parse("   "))
    }
}
