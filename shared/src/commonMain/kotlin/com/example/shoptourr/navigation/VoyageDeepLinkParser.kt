package com.example.shoptourr.navigation

/**
 * Parsed deep links from push / universal links / home-screen shortcuts.
 * Examples:
 * - `voyage://trips/{tripId}/alerts`
 * - `https://voyage.app/trips/{tripId}/tax-free`
 * - `voyage://purchases/new` (current trip)
 */
sealed class VoyageDeepLink {
    data class TripAlerts(val tripId: String) : VoyageDeepLink()
    data class TripTaxFree(val tripId: String) : VoyageDeepLink()
    data class TripDetail(val tripId: String) : VoyageDeepLink()
    data class TripRoute(val tripId: String) : VoyageDeepLink()
    data class AddPurchase(val tripId: String?) : VoyageDeepLink()
    data object Home : VoyageDeepLink()
}

object VoyageShortcutLinks {
    const val ADD_PURCHASE = "voyage://purchases/new"
    const val ADD_PURCHASE_TYPE = "com.shoptourr.addPurchase"
}

object VoyageDeepLinkParser {
    private val tripPath = Regex("""(?:voyage://|https?://[^/]+/)?trips/([^/]+)(?:/([a-z-]+))?/?""", RegexOption.IGNORE_CASE)
    private val addPurchasePath = Regex(
        """(?:voyage://|https?://[^/]+/)?(?:purchases(?:/new)?|add-purchase)/?""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(uri: String): VoyageDeepLink? {
        val trimmed = uri.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.equals("voyage://home", ignoreCase = true) ||
            trimmed.endsWith("/home")
        ) {
            return VoyageDeepLink.Home
        }
        if (addPurchasePath.matches(trimmed)) {
            return VoyageDeepLink.AddPurchase(tripId = null)
        }
        val match = tripPath.find(trimmed) ?: return null
        val tripId = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
        return when (match.groupValues.getOrNull(2)?.lowercase()) {
            "alerts" -> VoyageDeepLink.TripAlerts(tripId)
            "tax-free", "taxfree" -> VoyageDeepLink.TripTaxFree(tripId)
            "route", "map" -> VoyageDeepLink.TripRoute(tripId)
            "purchases", "purchase", "add-purchase" -> VoyageDeepLink.AddPurchase(tripId)
            null, "", "detail" -> VoyageDeepLink.TripDetail(tripId)
            else -> VoyageDeepLink.TripDetail(tripId)
        }
    }
}
