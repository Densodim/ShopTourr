package com.example.shoptourr.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PushDeepLinkExtractorTest {

    @Test
    fun `reads deep_link key`() {
        val uri = PushDeepLinkExtractor.extract(
            mapOf("deep_link" to "voyage://trips/lisbon/alerts"),
        )
        assertEquals("voyage://trips/lisbon/alerts", uri)
    }

    @Test
    fun `falls back to trip_id and screen`() {
        val uri = PushDeepLinkExtractor.extract(
            mapOf("trip_id" to "tokyo", "screen" to "tax-free"),
        )
        assertEquals("voyage://trips/tokyo/tax-free", uri)
    }

    @Test
    fun `returns null when payload empty`() {
        assertNull(PushDeepLinkExtractor.extract(emptyMap()))
    }
}

class VoyageDeepLinkRouterTest {

    @Test
    fun `maps alerts link to navigation target`() {
        val target = VoyageDeepLinkRouter.resolveUri("voyage://trips/lisbon/alerts")
        assertIs<VoyageNavigationTarget.TripAlerts>(target)
        assertEquals("lisbon", target.tripId)
    }

    @Test
    fun `maps push data through extractor`() {
        val target = VoyageDeepLinkRouter.resolvePushData(
            mapOf("deeplink" to "https://voyage.app/trips/oslo/route"),
        )
        assertIs<VoyageNavigationTarget.TripRoute>(target)
        assertEquals("oslo", target.tripId)
    }

    @Test
    fun `home push opens home`() {
        assertEquals(
            VoyageNavigationTarget.Home,
            VoyageDeepLinkRouter.resolvePushData(mapOf("url" to "voyage://home")),
        )
    }

    @Test
    fun `maps shortcut uri to add purchase for the current trip`() {
        val target = VoyageDeepLinkRouter.resolveUri("voyage://purchases/new")
        assertIs<VoyageNavigationTarget.AddPurchase>(target)
        assertNull(target.tripId)
    }

    @Test
    fun `maps trip purchase path to add purchase`() {
        val target = VoyageDeepLinkRouter.resolveUri("voyage://trips/lisbon/purchases")
        assertIs<VoyageNavigationTarget.AddPurchase>(target)
        assertEquals("lisbon", target.tripId)
    }

    @Test
    fun `maps purchase screen from push data`() {
        val target = VoyageDeepLinkRouter.resolvePushData(
            mapOf("trip_id" to "oslo", "screen" to "purchase"),
        )
        assertIs<VoyageNavigationTarget.AddPurchase>(target)
        assertEquals("oslo", target.tripId)
    }
}
