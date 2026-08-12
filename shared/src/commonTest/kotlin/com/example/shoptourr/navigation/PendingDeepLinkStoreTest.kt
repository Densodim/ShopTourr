package com.example.shoptourr.navigation

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class PendingDeepLinkStoreTest {

    @Test
    fun `offer then consume returns target once`() = runTest {
        val store = PendingDeepLinkStore()
        store.offer(VoyageNavigationTarget.TripAlerts("lisbon"))
        assertEquals(VoyageNavigationTarget.TripAlerts("lisbon"), store.consume())
        assertNull(store.consume())
    }

    @Test
    fun `observe emits pending target`() = runTest {
        val store = PendingDeepLinkStore()
        store.observe().test {
            assertNull(awaitItem())
            store.offer(VoyageNavigationTarget.Home)
            assertEquals(VoyageNavigationTarget.Home, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
