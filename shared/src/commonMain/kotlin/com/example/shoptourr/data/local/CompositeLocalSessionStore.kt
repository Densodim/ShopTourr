package com.example.shoptourr.data.local

import com.example.shoptourr.analytics.AnalyticsEventQueue
import com.example.shoptourr.data.push.DevicePushTokenHolder
import com.example.shoptourr.data.sync.SyncOutbox
import com.example.shoptourr.domain.repository.LocalSessionStore
import com.example.shoptourr.navigation.PendingDeepLinkStore

class CompositeLocalSessionStore(
    private val userLocalStore: UserLocalStore,
    private val tripLocalStore: TripLocalStore,
    private val purchaseLocalStore: PurchaseLocalStore,
    private val wishlistLocalStore: WishlistLocalStore,
    private val diaryLocalStore: DiaryLocalStore,
    private val taxFreeLocalStore: TaxFreeLocalStore,
    private val alertsLocalStore: AlertsLocalStore,
    private val routeLocalStore: RouteLocalStore,
    private val statsLocalStore: StatsLocalStore,
    private val exportLocalStore: ExportLocalStore,
    private val outbox: SyncOutbox,
    private val analyticsQueue: AnalyticsEventQueue? = null,
    private val pendingDeepLinks: PendingDeepLinkStore? = null,
) : LocalSessionStore {

    override suspend fun clearUserData() {
        tripLocalStore.clearAll()
        purchaseLocalStore.clearAll()
        wishlistLocalStore.clearAll()
        diaryLocalStore.clearAll()
        taxFreeLocalStore.clearAll()
        alertsLocalStore.clearAll()
        routeLocalStore.clearAll()
        statsLocalStore.clearAll()
        exportLocalStore.clearAll()
        outbox.clearAll()
        analyticsQueue?.clearAll()
        userLocalStore.clear()
        pendingDeepLinks?.clear()
        DevicePushTokenHolder.update(null)
    }
}
