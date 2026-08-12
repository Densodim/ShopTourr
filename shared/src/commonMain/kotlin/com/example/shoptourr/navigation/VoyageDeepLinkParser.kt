package com.example.shoptourr.navigation

/**
 * Parsed deep links from push / universal links.
 * Examples:
 * - `voyage://trips/{tripId}/alerts`
 * - `https://voyage.app/trips/{tripId}/tax-free`
 */
sealed class VoyageDeepLink {
    data class TripAlerts(val tripId: String) : VoyageDeepLink()
    data class TripTaxFree(val tripId: String) : VoyageDeepLink()
    data class TripDetail(val tripId: String) : VoyageDeepLink()
    data class TripRoute(val tripId: String) : VoyageDeepLink()
    data object Home : VoyageDeepLink()
}

object VoyageDeepLinkParser {
    private val tripPath = Regex("""(?:voyage://|https?://[^/]+/)?trips/([^/]+)(?:/([a-z-]+))?/?""", RegexOption.IGNORE_CASE)

    fun parse(uri: String): VoyageDeepLink? {
        val trimmed = uri.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.equals("voyage://home", ignoreCase = true) ||
            trimmed.endsWith("/home")
        ) {
            return VoyageDeepLink.Home
        }
        val match = tripPath.find(trimmed) ?: return null
        val tripId = match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
        return when (match.groupValues.getOrNull(2)?.lowercase()) {
            "alerts" -> VoyageDeepLink.TripAlerts(tripId)
            "tax-free", "taxfree" -> VoyageDeepLink.TripTaxFree(tripId)
            "route", "map" -> VoyageDeepLink.TripRoute(tripId)
            null, "", "detail" -> VoyageDeepLink.TripDetail(tripId)
            else -> VoyageDeepLink.TripDetail(tripId)
        }
    }
}
