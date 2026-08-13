package com.example.shoptourr.navigation

import org.koin.mp.KoinPlatform.getKoinOrNull

fun offerPendingDeepLinkUri(uri: String) {
    val store = getKoinOrNull()?.get<PendingDeepLinkStore>() ?: return
    VoyageDeepLinkRouter.resolveUri(uri)?.let(store::offer)
}
