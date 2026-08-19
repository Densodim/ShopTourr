package com.example.shoptourr.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shoptourr.domain.model.RouteStop

@Composable
actual fun VoyageNativeMap(
    stops: List<RouteStop>,
    caption: String?,
    modifier: Modifier,
) {
    RouteMapCanvas(stops = stops, caption = caption, modifier = modifier)
}
