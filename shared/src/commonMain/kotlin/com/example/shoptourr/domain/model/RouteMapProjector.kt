package com.example.shoptourr.domain.model

data class ProjectedMapPin(
    val stop: RouteStop,
    val x: Float,
    val y: Float,
)

/**
 * Layout helper for the stylized Voyage map (mila MapScreen).
 * Prefers geo bounds when lat/lng exist; otherwise uses the prototype index scatter.
 */
object RouteMapProjector {
    fun project(
        stops: List<RouteStop>,
        width: Float,
        height: Float,
        paddingFraction: Float = 0.12f,
    ): List<ProjectedMapPin> {
        if (stops.isEmpty() || width <= 0f || height <= 0f) return emptyList()
        val ordered = stops.sortedBy { it.orderIndex }
        val geo = ordered.mapNotNull { stop ->
            val lat = stop.point?.lat?.toDoubleOrNull()
            val lng = stop.point?.lng?.toDoubleOrNull()
            if (lat != null && lng != null) Triple(stop, lat, lng) else null
        }
        return if (geo.size == ordered.size && geo.isNotEmpty()) {
            projectGeo(geo, width, height, paddingFraction)
        } else {
            projectIndexed(ordered, width, height)
        }
    }

    private fun projectIndexed(
        stops: List<RouteStop>,
        width: Float,
        height: Float,
    ): List<ProjectedMapPin> =
        stops.mapIndexed { index, stop ->
            val xPct = 20 + ((index * 37) % 70)
            val yPct = 20 + ((index * 53) % 65)
            ProjectedMapPin(
                stop = stop,
                x = width * xPct / 100f,
                y = height * yPct / 100f,
            )
        }

    private fun projectGeo(
        points: List<Triple<RouteStop, Double, Double>>,
        width: Float,
        height: Float,
        paddingFraction: Float,
    ): List<ProjectedMapPin> {
        val lats = points.map { it.second }
        val lngs = points.map { it.third }
        val minLat = lats.min()
        val maxLat = lats.max()
        val minLng = lngs.min()
        val maxLng = lngs.max()
        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)
        val padX = width * paddingFraction
        val padY = height * paddingFraction
        val usableW = (width - padX * 2).coerceAtLeast(1f)
        val usableH = (height - padY * 2).coerceAtLeast(1f)
        return points.map { (stop, lat, lng) ->
            val nx = ((lng - minLng) / lngSpan).toFloat()
            val ny = (1.0 - (lat - minLat) / latSpan).toFloat() // north at top
            ProjectedMapPin(
                stop = stop,
                x = padX + nx * usableW,
                y = padY + ny * usableH,
            )
        }
    }
}
