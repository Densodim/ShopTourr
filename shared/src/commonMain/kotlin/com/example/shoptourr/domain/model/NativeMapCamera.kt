package com.example.shoptourr.domain.model

data class NativeMapPin(
    val id: String,
    val title: String,
    val lat: Double,
    val lng: Double,
)

/**
 * Camera + pins for a native map. Returns null when no stop has valid lat/lng
 * so the UI can keep the stylized [RouteMapProjector] canvas.
 */
data class NativeMapCamera(
    val centerLat: Double,
    val centerLng: Double,
    val latDelta: Double,
    val lngDelta: Double,
    val pins: List<NativeMapPin>,
) {
    companion object {
        private const val MIN_DELTA = 0.01
        private const val PAD = 1.4

        fun fromStops(stops: List<RouteStop>): NativeMapCamera? {
            val pins = stops.sortedBy { it.orderIndex }.mapNotNull { it.toPinOrNull() }
            if (pins.isEmpty()) return null
            val minLat = pins.minOf { it.lat }
            val maxLat = pins.maxOf { it.lat }
            val minLng = pins.minOf { it.lng }
            val maxLng = pins.maxOf { it.lng }
            return NativeMapCamera(
                centerLat = (minLat + maxLat) / 2.0,
                centerLng = (minLng + maxLng) / 2.0,
                latDelta = (maxLat - minLat).coerceAtLeast(MIN_DELTA) * PAD,
                lngDelta = (maxLng - minLng).coerceAtLeast(MIN_DELTA) * PAD,
                pins = pins,
            )
        }

        private fun RouteStop.toPinOrNull(): NativeMapPin? {
            val geo = point ?: return null
            val lat = geo.lat.toDoubleOrNull() ?: return null
            val lng = geo.lng.toDoubleOrNull() ?: return null
            if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
            return NativeMapPin(id = id, title = title, lat = lat, lng = lng)
        }
    }
}
