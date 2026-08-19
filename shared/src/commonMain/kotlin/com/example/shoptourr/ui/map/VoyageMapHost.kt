package com.example.shoptourr.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shoptourr.domain.model.RouteStop

/**
 * Honors the `nativeMaps` remote flag. Native SDKs stay off the classpath to keep
 * the 40 MiB budget; the expect/actual slot currently renders [RouteMapCanvas].
 */
@Composable
fun VoyageMapHost(
    stops: List<RouteStop>,
    caption: String? = null,
    nativeMapsEnabled: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (nativeMapsEnabled) {
        VoyageNativeMap(stops = stops, caption = caption, modifier = modifier)
    } else {
        RouteMapCanvas(stops = stops, caption = caption, modifier = modifier)
    }
}
