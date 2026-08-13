package com.example.shoptourr.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shoptourr.domain.model.RouteStop

/**
 * Map host: canvas fallback now; swap for MapKit / Google Maps via expect/actual later.
 */
@Composable
fun VoyageMapHost(
    stops: List<RouteStop>,
    caption: String? = null,
    nativeMapsEnabled: Boolean = false,
    modifier: Modifier = Modifier,
) {
    RouteMapCanvas(
        stops = stops,
        caption = if (nativeMapsEnabled) caption else caption,
        modifier = modifier,
    )
}
