package com.example.shoptourr.navigation

import org.koin.mp.KoinPlatform.getKoinOrNull

private val bufferedUris = mutableListOf<String>()

fun offerPendingDeepLinkUri(uri: String) {
    val store = getKoinOrNull()?.get<PendingDeepLinkStore>()
    if (store == null) {
        bufferedUris.add(uri)
        return
    }
    VoyageDeepLinkRouter.resolveUri(uri)?.let(store::offer)
}

fun offerPendingShortcut(type: String) {
    if (type.equals(VoyageShortcutLinks.ADD_PURCHASE_TYPE, ignoreCase = true)) {
        offerPendingDeepLinkUri(VoyageShortcutLinks.ADD_PURCHASE)
    }
}

fun flushPendingDeepLinkUris() {
    if (bufferedUris.isEmpty()) return
    val pending = bufferedUris.toList()
    bufferedUris.clear()
    pending.forEach(::offerPendingDeepLinkUri)
}
