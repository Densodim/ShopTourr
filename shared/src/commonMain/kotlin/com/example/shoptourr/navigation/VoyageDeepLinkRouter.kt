package com.example.shoptourr.navigation

/**
 * Navigation destinations resolved from deep links / push payloads.
 * UI layer maps these to Voyager screens.
 */
sealed class VoyageNavigationTarget {
    data object Home : VoyageNavigationTarget()
    data class TripDetail(val tripId: String) : VoyageNavigationTarget()
    data class TripAlerts(val tripId: String) : VoyageNavigationTarget()
    data class TripTaxFree(val tripId: String) : VoyageNavigationTarget()
    data class TripRoute(val tripId: String) : VoyageNavigationTarget()
    data class AddPurchase(val tripId: String?) : VoyageNavigationTarget()
}

object PushDeepLinkExtractor {
    private val uriKeys = listOf("deep_link", "deeplink", "link", "url")

    fun extract(data: Map<String, String>): String? {
        uriKeys.forEach { key ->
            data[key]?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        val tripId = (data["trip_id"] ?: data["tripId"])?.trim()?.takeIf { it.isNotEmpty() }
            ?: return null
        val screen = (data["screen"] ?: data["destination"])?.trim()?.lowercase()
        val suffix = when (screen) {
            "alerts", "alert" -> "/alerts"
            "tax-free", "taxfree", "tax_free" -> "/tax-free"
            "route", "map" -> "/route"
            "purchase", "purchases", "add-purchase", "add_purchase" -> "/purchases"
            "detail", "trip", null, "" -> ""
            else -> ""
        }
        return "voyage://trips/$tripId$suffix"
    }
}

object VoyageDeepLinkRouter {
    fun resolve(link: VoyageDeepLink): VoyageNavigationTarget = when (link) {
        VoyageDeepLink.Home -> VoyageNavigationTarget.Home
        is VoyageDeepLink.TripDetail -> VoyageNavigationTarget.TripDetail(link.tripId)
        is VoyageDeepLink.TripAlerts -> VoyageNavigationTarget.TripAlerts(link.tripId)
        is VoyageDeepLink.TripTaxFree -> VoyageNavigationTarget.TripTaxFree(link.tripId)
        is VoyageDeepLink.TripRoute -> VoyageNavigationTarget.TripRoute(link.tripId)
        is VoyageDeepLink.AddPurchase -> VoyageNavigationTarget.AddPurchase(link.tripId)
    }

    fun resolveUri(uri: String): VoyageNavigationTarget? =
        VoyageDeepLinkParser.parse(uri)?.let(::resolve)

    fun resolvePushData(data: Map<String, String>): VoyageNavigationTarget? {
        val uri = PushDeepLinkExtractor.extract(data) ?: return null
        return resolveUri(uri)
    }
}
